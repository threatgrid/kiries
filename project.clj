(defproject kiries "1.0.0-SNAPSHOT"
  :description "A bundled deployment of Riemann, and ElasticSearch"
  :dependencies [[clj-json "0.5.3"]
                 [com.taoensso/carmine "2.14.0"]
                 [clojurewerkz/elastisch "2.2.2"
                  :exclusions [clj-http]]
                 [com.taoensso/carmine "2.16.0"]
                 [org.clojure/clojure "1.8.0"]
                 [cheshire "5.7.0"]
                 [org.clojure/tools.reader "1.0.3"]
                 [org.clojars.jcsims/riemann "0.2.16"]]
  :main riemann.bin
  :profiles {:uberjar {:aot :all,}}
  :aliases {"server" ["trampoline" "run" "-m" "riemann.bin" "config/riemann.config"]})
