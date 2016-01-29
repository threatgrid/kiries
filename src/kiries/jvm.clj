(ns kiries.jvm
  (:import [java.util.concurrent TimeUnit]
           [com.codahale.metrics Clock JvmAttributeGaugeSet]
           [com.codahale.metrics.jvm MemoryUsageGaugeSet ThreadStatesGaugeSet
            FileDescriptorRatioGauge GarbageCollectorMetricSet]))

(def clock (Clock/defaultClock))

(def memory-stats (MemoryUsageGaugeSet.))

(def jvm-stats (JvmAttributeGaugeSet.))

(def thread-stats (ThreadStatesGaugeSet.))

(def fd-stats (FileDescriptorRatioGauge.))

(def gc-stats (GarbageCollectorMetricSet.))

(keys (.getMetrics gc-stats))

(defn vm-metrics [& {:keys [:prefix]}]
  (let [epoch (long (/ (.getTime clock) 1000))
        prefix (when prefix
                 (if (or (.endsWith prefix ".") (empty? prefix))
                   prefix
                   (str prefix ".")))
        memory-metrics (.getMetrics memory-stats)
        jvm-metrics (.getMetrics jvm-stats)
        thread-metrics (.getMetrics thread-stats)
        gc-metrics (.getMetrics gc-stats)
        make-entry (fn [name metric]
                     {:service (str prefix name)
                      :metric (.getValue metric)
                      :time epoch})]
    (concat [(make-entry "jvm.memory.heap-usage"
                         (get memory-metrics "heap.used"))
             (make-entry "jvm.memory.non-heap-usage"
                         (get memory-metrics "non-heap.used"))
             (make-entry "jvm.thread.daemon-count"
                         (get thread-metrics "daemon.count"))
             (make-entry "jvm.thread.count"
                         (get thread-metrics "count"))
             (make-entry "jvm.uptime"
                         (get jvm-metrics "uptime"))
             (make-entry "jvm.fd-usage"
                         fd-stats)]
            (for [k (filter #(and (.startsWith % "pools.")
                                  (.endsWith % ".usage"))
                            (keys memory-metrics))]
              (make-entry (str "jvm.memory.pool-usage."
                               (subs k 6 (- (.length k) 6)))
                          (get memory-metrics k)))
            (for [k (Thread$State/values)]
              (let [key (.toLowerCase (str k))]
                (make-entry (str "jvm.thread.state." key)
                            (get thread-metrics (str key ".count")))))
            (for [k (filter #(.endsWith % ".time")
                            (keys gc-metrics))]
              (make-entry (str "jvm.gc." k)
                          (get gc-metrics k)))
            (for [k (filter #(.endsWith % ".count")
                            (keys gc-metrics))]
              (make-entry (str "jvm.gc." (subs k 0 (- (.length k) 6)) ".runs")
                          (get gc-metrics k))))))
