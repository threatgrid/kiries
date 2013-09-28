(defproject kiries "0.1.0-SNAPSHOT"
  :description "A bundled deployment of Kibana, Riemann, and ElasticSearch"
  :license "Eclipse Public License v1.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.cli "0.2.1"]
                 [clj-logging-config "1.9.6"]
                 
                 [riemann "0.2.2"]
                 [clj-time "0.6.0"]
                 [clojurewerkz/elastisch "1.2.0"]
                 [org.elasticsearch/elasticsearch "0.90.3"]
                 
                 [compojure "1.1.5"]
                 [hiccup "1.0.4"]
                 [org.markdownj/markdownj "0.3.0-1.0.2b4"]
                 [ring/ring-core "1.2.0"]
                 [ring/ring-devel "1.2.0"]
                 [ring/ring-jetty-adapter "1.2.0"]
                 
                 [clojure-csv/clojure-csv "1.3.2"]
                 ]
  :resource-paths ["." "resources"]
  :jar-exclusions [#"^htdocs"]
  :main kiries.core
  :aliases {"server" ["trampoline" "run" "-m" "kiries.core"]})


