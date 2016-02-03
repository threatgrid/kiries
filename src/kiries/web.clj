(ns kiries.web
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [kiries.layout :as layout]
            [ring.middleware.defaults :as ring-defaults]
            [ring.middleware.reload :as reload]
            [ring.util.response :as resp])
  (:import [com.petebevin.markdown MarkdownProcessor]))


(defn mdwn
  "Given a set of string fragments, generate markdown from them.  A newline is placed in between them."
  [& fragments]
  (.markdown (MarkdownProcessor.)
             (apply str (interpose "\n" fragments))))

(defn markdown-resource-as-html [file]
  (let [raw (slurp file)] (mdwn raw)))

(defn doc-html [file]
  (let [file (clojure.string/replace file ".." "")]
    (markdown-resource-as-html (clojure.java.io/resource (str "doc/" file ".md")))))

(defroutes main-routes
  (GET "/" []  (resp/redirect "/index.html"))
  (GET "/index.html" []
       (layout/full-layout "Welcome to Kiries"
                           (markdown-resource-as-html
                            (clojure.java.io/resource "htdocs/index.md"))))

  (GET "/doc/:file.html" [file] (doc-html file))

  ;; Map kibana into web space
  (route/resources "/kibana/" {:root "htdocs/kibana"})

  ;; Map css into web space
  (route/resources "/css/" {:root "htdocs/css"})

  ;; Point to our bundled installation of ElasticSearch HQ
  (route/resources "/HQ/" {:root "htdocs/royrusso-elasticsearch-HQ-ea630c8"})


  (route/not-found "<h1>Page not found</h1>"))

(def app
  (reload/wrap-reload (ring-defaults/wrap-defaults main-routes ring-defaults/site-defaults)))

(defn start [& {:keys [port host
                       ssl ssl-port
                       keystore key-password
                       truststore trust-password
                       join?]
                :or {port 9090
                     host "0.0.0.0"
                     join? true
                     }
                :as options}]
  (require 'ring.adapter.jetty)
  (let [run-fn (resolve 'ring.adapter.jetty/run-jetty)] ;; force runtime resolution of jetty
    (run-fn #'app
            {:host host
             :port port
             :join? join?
             :ssl ssl
             :ssl-port ssl-port
             :keystore keystore
             :key-password key-password
             :truststore trust-password})))
