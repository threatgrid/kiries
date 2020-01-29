(ns kiries.elastic
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [clj-time.format]
            [clj-time.core]
            [clj-time.coerce]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [clojurewerkz.elastisch.rest.bulk :as eb]
            [clojurewerkz.elastisch.rest :as esr]
            [riemann.streams :as streams]))

(defn make-index-timestamper [index period]
  (let [formatter (clj-time.format/formatter (str "'" index "'"
                                  (cond
                                   (= period :day)
                                   "-YYYY.MM.dd"
                                   (= period :hour)
                                   "-YYYY.MM.dd.HH"
                                   (= period :week)
                                   "-YYYY.MM.dd.ww"
                                   (= period :month)
                                   "-YYYY.MM"
                                   (= period :year)
                                   "-YYYY")))]
    (fn [date]
      (clj-time.format/unparse formatter date))))

(def get-elasticsearch-version
  (memoize (fn [url]
             (-> (http/get url)
                 :body
                 (json/decode keyword)
                 (get-in [:version :number])
                 (string/split #"\D+")
                 (->> (map #(Integer/parseInt %)))))))

(defn- get-major-version [connection]
  (first (get-elasticsearch-version (:uri connection))))

(def ^{:private true} format-iso8601
  (clj-time.format/with-zone (clj-time.format/formatters :date-time)
    clj-time.core/utc))

(defn ^{:private true} iso8601 [event-s]
  (clj-time.format/unparse format-iso8601
                           (clj-time.coerce/from-long (* 1000 event-s))))

(defn ^{:private true} safe-iso8601 [event-s]
  (try (iso8601 event-s)
    (catch Exception e
      (log/warn "Unable to parse iso8601 input: " event-s)
      (clj-time.format/unparse format-iso8601 (clj-time.core/now)))))

(defn ^{:private true} stashify-timestamp [event]
  (-> (if-not (get event "@timestamp")
        (assoc event "@timestamp"
               (if-let [isotime (:isotime event)]
                 isotime
                 (if-let [time (:time event)]
                   (safe-iso8601 (long time))
                   (clj-time.format/unparse format-iso8601 (clj-time.core/now)))))
         event)
      (dissoc :isotime :time :ttl)))

(defn ^{:private true} edn-safe-read [v]
  (try
    (edn/read-string v)
    (catch Exception e
      (log/warn "Unable to read supposed EDN form with value: " v)
      v)))

(defn ^{:private true} massage-event [event]
  (into {}
        (for [[k v] event
              :when v]
          (cond
           (= (name k) "_id") [k v]
           (.startsWith (name k) "_")
           [(.substring (name k) 1) (edn-safe-read v)]
           :else
           [k v]))))

(defn ^{:private true} elastic-event [event massage]
  (let [e (-> event
              stashify-timestamp)]
    (if massage
      (massage-event e)
      e)))

(defn ^{:private true} riemann-to-elasticsearch [events massage]
  (->> [events]
       flatten
       (remove streams/expired?)
       (map #(elastic-event % massage))))

(defn es-connect
  "Connects to the ElasticSearch node.

  The first optional argument is a url for the node, which defaults to
  `http://localhost:9200`.  The second optional arg is a map of
  options to be passed to clj-http.

  This must be called before any es-* functions can be used."
  ([]
   (es-connect "http://localhost:9200"))
  ([host]
   (es-connect host nil))
  ([host http-opts]
   (esr/connect host
                (merge {:content-type "application/x-ndjson"}
                       http-opts))))

(defn- parse-1x-bulk-results [res]
  {:total (count (:items res))
   :succ (filter #(= 201 (get-in % [:create :status])) (:items res))
   :failed (filter :error (:items res))})

(defn- parse-5x-bulk-results [res]
  {:total (count (:items res))
   :succ (filter #(= 201 (get-in % [:index :status])) (:items res))
   :failed (remove #(get-in % [:index "created"]) (:items res))})

(defn- parse-6x-bulk-results [res]
  {:total (count (:items res))
   :succ (filter #(= 201 (get-in % [:index :status])) (:items res))
   :failed (remove #(= "created" (get-in % [:index :result])) (:items res))})

(defn- parse-bulk-results [major-version res]
  (case major-version
    (1 2) (parse-1x-bulk-results res)
    5 (parse-5x-bulk-results res)
    6 (parse-6x-bulk-results res)))

(defn es-index
  "A function which takes a sequence of events, and indexes them in
  ElasticSearch.  It will set the `_type` field of the event to the
  value of `doc-type`, which tells ES which mapping to use for the
  event.

  The :index argument defaults to \"logstash\" for Kibana
  compatability.  It's the root ES index name that this event will be
  indexed within.

  The :timestamping argument, default to :day and it controls the time
  range component of the index name.  Acceptable values
  are :hour, :day, :week, :month and :year.

  Events will be massages to conform to Kibana expections.  This means
  that the `@timestamp` field will be set if not found, based on the
  `time` field of the event.  The `ttl` field will be removed, as it's
  internal to Riemann.  Lastly, any fields starting with an `_` will
  have their value parsed as EDN.
"
  [connection doc-type & {:keys [index timestamping massage]
                          :or {index "logstash"
                               massage true
                               timestamping :day}}]
  (let [index-namer (make-index-timestamper index timestamping)]
    (fn [events]
      (let [esets (group-by (fn [e]
                              (index-namer
                               (clj-time.format/parse format-iso8601
                                                      (get e "@timestamp"))))
                            (riemann-to-elasticsearch events massage))]
        (doseq [index (keys esets)]
          (let [raw (get esets index)
                bulk-create-items
                (interleave (map #(if-let [id (get % "_id")]
                                    {:create {:_type doc-type :_id id}}
                                    {:index {:_type doc-type}})
                                 raw)
                            raw)]
            (when (seq bulk-create-items)
              (try
                (let [res (eb/bulk-with-index connection index bulk-create-items)
                      {:keys [total succ failed]}
                      (parse-bulk-results (get-major-version connection) res)]
                  (log/info (str "elasticized " total "/" (count succ) "/" (- total (count succ))
                                 " (total/succ/fail) items to index " index " in " (:took res) " ms"))
                  (log/debug "Failed: " failed))
                (catch Exception e
                  (log/error "Unable to bulk index:" e))))))))))
