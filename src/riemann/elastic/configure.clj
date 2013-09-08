(ns riemann.elastic.configure
  (:require [clojurewerkz.elastisch.rest :as api]
            [clojurewerkz.elastisch.rest :as esr]
            [cheshire.custom :as json]
            [clojure.java.io :as io]))

;; ----------------------------------------
;; connection

(def elastic-host (or (System/getenv "ELASTIC_HOST") "localhost"))
(def elastic-port (Integer/parseInt (or (System/getenv "ELASTIC_PORT") "9200")))

(defn elastic-url []
  (str "http://" elastic-host ":" elastic-port "/"))

(defn connect! []
  (esr/connect! (elastic-url)))

;; ----------------------------------------
;; index creation
(defn resource-as-json [resource-name]
  (json/decode (slurp (io/resource resource-name))))

(defn create-index-template [template-name mapping-file]
  (api/put (api/index-template-url template-name)
           :body (resource-as-json mapping-file)))


;; ----------------------------------------
;; all configuration
(defn -main [& args]
  (connect!)
  (create-index-template "face" "mapping.json"))
