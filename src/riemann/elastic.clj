(ns riemann.elastic
  (:use [clojure.tools.logging :only (info error debug warn)])
  (:require [clj-json.core :as json]
            [clj-time.format]
            [clj-time.core]
            [clj-time.coerce]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
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

(def ^{:private true} format-iso8601
  (clj-time.format/with-zone (clj-time.format/formatters :date-time)
    clj-time.core/utc))

(defn ^{:private true} iso8601 [event-s]
  (clj-time.format/unparse format-iso8601
                           (clj-time.coerce/from-long (* 1000 event-s))))

(defn ^{:private true} safe-iso8601 [event-s]
  (try (iso8601 event-s)
    (catch Exception e
      (warn "Unable to parse iso8601 input: " event-s)
      (clj-time.format/unparse format-iso8601 (clj-time.core/now)))))

(defn ^{:private true} stashify-timestamp [event]
  (->  (if-not (get event "@timestamp")
         (let [isotime (:isotime event)]
           (if-not isotime
             (let [time (:time event)]
               (assoc event "@timestamp" (safe-iso8601 (long time))))
             (assoc event "@timestamp" isotime)))
         event)
       (dissoc :isotime)
       (dissoc :time)
       (dissoc :ttl)))

(defn ^{:private true} edn-safe-read [v]
  (try
    (edn/read-string v)
    (catch Exception e
      (warn "Unable to read supposed EDN form with value: " v)
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
  "Connects to the ElasticSearch node.  The optional argument is a url
  for the node, which defaults to `http://localhost:9200`.  This must
  be called before any es-* functions can be used."
  [& argv]
  (esr/connect! (or (first argv) "http://localhost:9200")))

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
  [doc-type & {:keys [index timestamping massage]
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
                                    {:create {:_type doc-type}}
                                    )
                                 raw)
                            raw)]
            (when (seq bulk-create-items)
              (try
                (let [res (eb/bulk-with-index index bulk-create-items)
                      total (count (:items res))
                      succ (filter :ok (:items res))
                      failed (filter :error (:items res))]
                  
                  (info "elasticized" total "/" (count succ) "/" (count failed) " (total/succ/fail) items to index " index "in " (:took res) "ms")
                  (debug "Failed: " failed))
                (catch Exception e
                  (error "Unable to bulk index:" e))))))))))

(defn ^{:private true} resource-as-json [resource-name]
  (json/parse-string (slurp (io/resource resource-name))))


(defn ^{:private true} file-as-json [file-name]
  (try
    (json/parse-string (slurp file-name))
    (catch Exception e
      (error "Exception while reading JSON file: " file-name)
      (throw e))))


(defn load-index-template 
  "Loads the file into ElasticSearch as an index template."
  [template-name mapping-file]
  (esr/put (esr/index-template-url template-name)
           :body (file-as-json mapping-file)))
