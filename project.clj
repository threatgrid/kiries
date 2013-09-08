(defproject riemann-custom "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [riemann "0.2.2"]
                 [clojurewerkz/elastisch "1.2.0"]
                 [clj-time "0.6.0"]]
  :main riemann.bin
  :aliases {"elastic" ["trampoline" "run" "-m" "riemann.elastic.configure"]})


