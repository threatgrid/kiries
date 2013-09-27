(defproject kiries "0.1.0-SNAPSHOT"
  :description "A push-button deplyment of Kibana3, Riemann, and ElasticSearch"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.cli "0.2.1"]
                 [riemann "0.2.2"]
                 [clojurewerkz/elastisch "1.2.0"]
                 [org.elasticsearch/elasticsearch "0.90.3"]
                 [compojure "1.1.5"]
                 [ring/ring-core "1.2.0"]
                 [ring/ring-jetty-adapter "1.2.0"]
                 [clj-time "0.6.0"]]
  :classpath ".:resources"
  :main kiries.core
  :aliases {"elastic" ["trampoline" "run" "-m" "riemann.elastic.configure"]})


