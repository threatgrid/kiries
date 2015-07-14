(defproject kiries "0.1.0-SNAPSHOT"
  :description "A bundled deployment of Kibana, Riemann, and ElasticSearch"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.cli "0.2.4"]

                 [riemann "0.2.9"]

                 [clj-json "0.5.3"]
                 [slingshot "0.12.1"]
                 
                 [clj-time "0.9.0"]
                 [clojurewerkz/elastisch "1.5.0-beta3"]
                 [org.elasticsearch/elasticsearch "1.1.1"]
                 
                 [metrics-clojure "1.0.1"]
                 
                 ;; web tools
                 [compojure "1.1.8"]
                 [hiccup "1.0.5"]
                 [org.markdownj/markdownj "0.3.0-1.0.2b4"]
                 [ring/ring-core "1.2.2"]
                 [ring/ring-devel "1.2.2"]
                 [ring/ring-jetty-adapter "1.2.2"]

                 ;; integration tools
                 [clj-http "1.0.1"]
                 [cheshire "5.5.0"]
                 [clojure-csv/clojure-csv "2.0.1"]

                 ;; Redis integration
                 [com.taoensso/carmine "2.6.2"]

                 ;; Resolve dependency version conflicts explicitly
                 [org.clojure/tools.nrepl "0.2.10"]
                 [org.clojure/tools.reader "0.8.10"]
                 [com.taoensso/encore "1.11.2"]
                 [org.codehaus.plexus/plexus-utils "3.0"]
                 [com.taoensso/nippy "2.7.0"]
                 [clj-logging-config "1.9.12"]
                ]
  
  :resource-paths ["resources"]
  ;;:jar-exclusions [#"^config"]
  :main kiries.core
  :profiles {:uberjar {:aot :all, :uberjar-merge-with {#"^META-INF/services/" [slurp str spit]}}}
  :aliases {"server" ["trampoline" "run" "-m" "kiries.core"]})


