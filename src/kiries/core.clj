(ns kiries.core
  (:gen-class)
  (:use [clojure.tools.cli]
        [clojure.tools.logging :only (info error debug warn)])
  (:require [riemann.config :as ri-config]
            [riemann.logging :as ri-logging]
            [riemann.time :as ri-time]
            [riemann.bin :as ri-bin]
            [riemann.config :as ri-config]
            [riemann.pubsub :as ri-pubsub]
            [kiries.web :as web])
  (:import org.elasticsearch.node.NodeBuilder))

(defonce es-service (atom nil))

(defn start-es []
  (swap! es-service
         (fn [old]
           (.node (NodeBuilder/nodeBuilder)))))

(defn stop-es []
  (.close (es-service)))


(defn start-ri [& argv]
  (ri-logging/init)
  (reset! ri-bin/config-file (or (first argv) "config/riemann.config"))
  (ri-bin/handle-signals)
  (ri-time/start!)
  (ri-config/include @ri-bin/config-file)
  (ri-config/apply!))

(defn stop-ri []
  (ri-time/stop!))

(defn reload-ri []
  (ri-bin/reload!))

(defn -main [& args]
  (let [[options args banner]
        (cli args
             ["-?" "--help" "Show help" :default false :flag true]
             ["-h" "--host" "Interface to listen on." :default "0.0.0.0"]
             ["-p" "--port" "Port to listen on." :default 9090 :parse-fn #(Integer. %)]
             ["-e" "--[no-]elasticsearch" "Run ES internally." :default true :flag true]
             ["-r" "--[no-]riemann" "Run Riemann internally." :default true :flag true]
             ["-w" "--[no-]web" "Run webserver/kibana internally." :default true :flag true]
             )]
    (when (:help options)
      (do
        (println banner)
        (System/exit 0)))
    
    (when (:elasticsearch options)
      (print "Starting elasticsearch")
      (start-es))
    
    (when (:riemann options)
      (print "Starting Riemann")
      (start-ri))
    
    (when (:web options)
      (print "Starting web server for Kibana")
      (web/start :port (:port options)
                 :join? false
                 :host (:host options)))))
    







  



