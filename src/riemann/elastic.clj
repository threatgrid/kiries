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

(def format-iso8601
  (clj-time.format/with-zone (clj-time.format/formatters :date-time-no-ms)
    clj-time.core/utc))

(defn iso8601 [event-s]
  (clj-time.format/unparse format-iso8601
                           (clj-time.coerce/from-long (* 1000 event-s))))

(defn safe-iso8601 [event-s]
  (try (iso8601 event-s)
    (catch Exception e
      (warn "Unable to parse iso8601 input: " event-s)
      (clj-time.format/unparse format-iso8601 (clj-time.core/now)))))

(defn stashify-timestamp [event]
  (let [time (:time event)]
    (-> event
        (dissoc :time)
        (dissoc :ttl)
        (assoc "@timestamp" (safe-iso8601 (long time))))))

(defn edn-safe-read [v]
  (try
    (edn/read-string v)
    (catch Exception e
      (warn "Unable to read supposed EDN form: " v)
      "UNREADABLE")))

(defn massage-event [event]
  (into {}
        (for [[k v] event
              :when v]
          (cond
           (.startsWith (name k) "_")
           [(.substring (name k) 1) (edn-safe-read v)]
           :else
           [k v]))))

(defn elastic-event [event]
  (-> event
      massage-event
      stashify-timestamp))

(defn riemann-to-elasticsearch [events]
  (->> [events]
       flatten
       (remove streams/expired?)
       (map elastic-event)))

(defn es-connect [& argv]
  (esr/connect! (or (first argv) "http://localhost:9200")))

(defn es-index [doc-type & {:keys [index
                                   timestamping]
                            :or {index "logstash"
                                 timestamping :day}}]
  (let [index-namer (make-index-timestamper index timestamping)]
    (fn [events]
      (let [index (index-namer (clj-time.core/now))
            bulk-create-items (interleave (repeat {:create {:_type doc-type}})
                                          (riemann-to-elasticsearch events))]
        (when (seq bulk-create-items)
          (let [res (eb/bulk-with-index index bulk-create-items)]
            (info "elasticized" (count (:items res)) "items in " (:took res) "ms")
            res))))))

(defn resource-as-json [resource-name]
  (json/parse-string (slurp (io/resource resource-name))))


(defn file-as-json [file-name]
  (try
    (json/parse-string (slurp file-name))
    (catch Exception e
      (error "Exception while reading JSON file: " file-name)
      (throw e))))


(defn load-index-template [template-name mapping-file]
  (esr/put (esr/index-template-url template-name)
           :body (file-as-json mapping-file)))





