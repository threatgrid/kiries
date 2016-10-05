(ns kiries.core
  (:gen-class)
  (:require [clojure.tools.cli :as cli]
            [clojure.tools.logging :as log]
            [kiries.web :as web]
            [riemann.bin :as ri-bin]
            [riemann.config :as ri-config]
            [riemann.logging :as ri-logging]
            [riemann.pubsub :as ri-pubsub]
            [riemann.time :as ri-time])
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
  (ri-bin/set-config-file! (or (first argv) "config/riemann.config"))
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
        (cli/cli args
                 ["-?" "--help" "Show help" :default false :flag true]
                 ["-h" "--host" "Interface to listen on." :default "0.0.0.0"]
                 ["-p" "--port" "Port to listen on." :default 9090 :parse-fn #(Integer. %)]
                 ["-e" "--[no-]elasticsearch" "Run ES internally." :default true :flag true]
                 ["-r" "--[no-]riemann" "Run Riemann internally." :default true :flag true]
                 ["-w" "--[no-]web" "Run webserver/kibana internally." :default true :flag true])]
    (when (:help options)
      (println banner)
      (System/exit 0))

    (when (:elasticsearch options)
      (log/info "Starting elasticsearch\n")
      (start-es))

    (when (:riemann options)
      (log/info "Starting Riemann\n")
      (start-ri))

    (when (:web options)
      (log/info "Starting web server for Kibana\n")
      (web/start :port (:port options)
                 :join? false
                 :host (:host options)))))
