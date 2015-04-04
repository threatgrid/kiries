(defproject kiries "0.1.0-SNAPSHOT"
  :description "A bundled deployment of Kibana, Riemann, and ElasticSearch"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.cli "0.2.4"]
                 [clj-logging-config "1.9.10"]
                 [clj-json "0.5.3"]
                 [slingshot "0.10.3"]
                 
                 [riemann "0.2.5"]
                 [clj-time "0.7.0"]
                 [clojurewerkz/elastisch "1.5.0-beta3"]
                 [org.elasticsearch/elasticsearch "1.1.1"]
                 
                 [metrics-clojure "1.0.1"]
                 
                 ;; web tools
                 [compojure "1.1.8"]
                 [hiccup "1.0.5"]
                 [org.markdownj/markdownj "0.3.0-1.0.2b4"]
                 [ring/ring-core "1.2.0"]
                 [ring/ring-devel "1.2.0"]
                 [ring/ring-jetty-adapter "1.2.0"]

                 ;; integration tools
                 [clj-http "0.9.1"]
                 [cheshire "5.3.1"]
                 [clojure-csv/clojure-csv "2.0.1"]

                 ;; Redis integration
                 [com.taoensso/carmine "2.6.2"]]
  
  :resource-paths ["resources"]
  ;;:jar-exclusions [#"^config"]
  :main kiries.core
  :profiles {:uberjar {:aot :all, :uberjar-merge-with {#"^META-INF/services/" [slurp str spit]}}}
  :aliases {"server" ["trampoline" "run" "-m" "kiries.core"]})


