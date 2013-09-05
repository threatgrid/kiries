(ns riemann.elastic
  (:require [riemann.streams :as streams]
            [clj-time.format]
            [clj-time.core]
            [clj-time.coerce]
            [clojurewerkz.elastisch.rest.bulk :as eb]
            [clojurewerkz.elastisch.rest :as esr]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.rest.index :as esi]
            [clojurewerkz.elastisch.rest.response :as esrsp]))


(def format-logstash
  (clj-time.format/formatter "'logstash'-yyyy.MM.dd"))
(def format-iso8601
  (clj-time.format/with-zone (clj-time.format/formatters :date-time-no-ms)
    clj-time.core/utc))

(defn index-name-for-date []
  (clj-time.format/unparse format-logstash (clj-time.core/now)))

(defn iso8601 [event-s]
  (clj-time.format/unparse format-iso8601
                           (clj-time.coerce/from-long (* 1000 event-s))))

(def last-event (atom nil))

(defn stashify-timestamp [event]
  (let [time (:time event)]
    (-> event
        (dissoc :time)
        (dissoc :ttl)
        (assoc "@timestamp" (iso8601 time)))))

(defn elastic-event [event]
  (reset! last-event event)
  (-> (into {} event)
      ;; strip nil values
      stashify-timestamp))

(defn riemann-to-elasticsearch [events]
  (->> [events]
       flatten
       (remove streams/expired?)
       (map elastic-event)))

(defn elasticsearch [url]
  (esr/connect! url)
  (fn [es-type]
    (fn [events]
      (let [es-index (index-name-for-date)
            bulk-create-items (interleave (repeat {:create {:_type es-type}})
                                          (riemann-to-elasticsearch events))]
        (when (seq bulk-create-items)
          (let [res (eb/bulk-with-index es-index bulk-create-items)]
            (println "!elasticized" (count (:items res)) "items in " (:took res) "ms")
            res))))))





