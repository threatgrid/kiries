(ns kiries.web
  (:use compojure.core)
  (:use ring.middleware.resource)
  (:use ring.middleware.reload)
  (:use ring.adapter.jetty)
  (:import com.petebevin.markdown.MarkdownProcessor)
  (:require
   [kiries.layout :as layout]
   [ring.util.response :as resp]
   [compojure.route :as route]
   [compojure.handler :as handler]))


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

  ;; Point to our bundled installation of ElasticSearch HQ
  (route/resources "/HQ/" {:root "htdocs/royrusso-elasticsearch-HQ-c321806"})


  (route/not-found "<h1>Page not found</h1>"))

(def app
  (wrap-reload (handler/site main-routes)))

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
  (run-jetty #'app
             {:host host
              :port port
              :join? join?
              :ssl ssl
              :ssl-port ssl-port
              :keystore keystore
              :key-password key-password
              :truststore trust-password}))
