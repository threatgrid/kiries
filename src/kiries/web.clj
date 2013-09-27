(ns kiries.web
  (:use compojure.core)
  (:use ring.middleware.resource)
  (:use ring.middleware.reload)
  (:use ring.adapter.jetty)
  (:require
   [compojure.route :as route]
   [compojure.handler :as handler]))

(defroutes main-routes
  (route/resources "/" {:root "htdocs"})
  (route/not-found "<h1>Page not found</h1>"))

(def app
  (wrap-reload (handler/site main-routes)))

(defn start [& {:keys [port host
                       ssl ssl-port
                       keystore key-password
                       truststore trust-password
                       join?]
                :or {port 8080
                     host "0.0.0.0"
                     join? true
                     }
                :as options}]
  (run-jetty #'app options))
