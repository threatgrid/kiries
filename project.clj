(defproject kiries "0.1.0-SNAPSHOT"
  :description "A bundled deployment of Kibana, Riemann, and ElasticSearch"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.cli "0.3.3"]

                 [riemann "0.2.10"]

                 [clj-json "0.5.3"]

                 [clj-time "0.11.0"]
                 [clojurewerkz/elastisch "2.2.1"]

                 [org.elasticsearch/elasticsearch "1.7.4"]
                 [org.apache.lucene/lucene-expressions "4.10.4"]
                 [org.codehaus.groovy/groovy-all "2.3.2"]

                 [metrics-clojure "2.6.1"]
                 [io.dropwizard.metrics/metrics-core "3.1.2"]
                 [io.dropwizard.metrics/metrics-jvm "3.1.2"]

                 ;; web tools
                 [compojure "1.4.0"]
                 [hiccup "1.0.5"]
                 [org.markdownj/markdownj-core "0.4"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-devel "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring/ring-jetty-adapter "1.4.0"]

                 ;; Redis integration
                 [com.taoensso/carmine "2.12.2"]]

  :resource-paths ["resources"]
  ;;:jar-exclusions [#"^config"]
  :main kiries.core
  :profiles {:uberjar {:aot :all, :uberjar-merge-with {#"^META-INF/services/" [slurp str spit]}}}
  :aliases {"server" ["trampoline" "run" "-m" "kiries.core"]})


