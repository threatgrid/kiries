(ns kiries.redis
  (:use [clojure.tools.logging :only (info error debug warn)])
  (:require [taoensso.carmine :as car]
            [clj-json.core :as json]))

(defn publish
  "Returns a function which publishes a single event as JSON, to the
  spcified redis server and key."
  [conn key]
  (let [f (spublish conn key)]
    (fn [e]
      (f [e]))))

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
                            (error "Could not coerce event to json (" e "):" err)
                            nil)))
                   events))]
      (doseq [je jevents]
        (try
          (car/wcar conn
                    (car/publish key je)
                    )
          (catch Exception e
            (warn "Unable to publish" key "to" conn ":" e)))))))
