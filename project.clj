(defproject kiries "1.0.0-SNAPSHOT"
  :description "A bundled deployment of Riemann, and ElasticSearch"
  :dependencies [[clj-json "0.5.3"]
                 [clojurewerkz/elastisch "2.2.2"]
                 [com.taoensso/carmine "2.14.0"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.reader "1.0.0-beta3"]
                 [riemann "0.2.11"]]
  :main riemann.bin
  :profiles {:uberjar {:aot :all,}}
  :aliases {"server" ["trampoline" "run" "-m" "riemann.bin" "config/riemann.config"]})
