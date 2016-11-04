(ns kiries.redis
  (:require [clojure.tools.logging :as log]
            [taoensso.carmine :as car]
            [clj-json.core :as json]))

(defn key-lengths
  "Given a redis connection and a pattern ('*' wildcard), return a map
  of key name to key length."
  [conn patt]
  (let [keys (car/wcar conn (car/keys patt))]
    (zipmap keys (car/wcar conn (doseq [key keys] (car/llen key))))))

(defn spublish
  "Returns a function which takes a sequence of events or other
   values, and publishes them as JSON, to the specified redis server
   and key.  Suitable for using inside of rollup."
  [conn key]
  (fn [events]
    (let [jevents
          (remove nil?
                  (map
                   (fn [e]
                     (try (json/generate-string e)
                          (catch Exception err
                            (log/error "Could not coerce event to json (" e "):" err)
                            nil)))
                   events))]
      (doseq [je jevents]
        (try
          (car/wcar conn
            (car/publish key je))
          (catch Exception e
            (log/warn "Unable to publish" key "to" conn ":" e)))))))

(defn publish
  "Returns a function which publishes a single event as JSON, to the
  spcified redis server and key."
  [conn key]
  (let [f (spublish conn key)]
    (fn [e]
      (f [e]))))
