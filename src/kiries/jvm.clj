(ns kiries.jvm
  (:import [com.yammer.metrics.core MetricPredicate]))

;; ----------------------------------------

(def clock (com.yammer.metrics.core.Clock/defaultClock))
(def vm-stats (com.yammer.metrics.core.VirtualMachineMetrics/getInstance))

(defn vm-metrics [& {:keys [:prefix]}]
  (let [epoch
        (long (/ (.time clock) 1000))

        prefix (if prefix
                  (if (or (.endsWith prefix ".") (empty? prefix))
                    prefix
                    (str prefix "."))
                  "")

        metrics
        (concat
         [{:service "jvm.memory.heap-usage" :metric (.heapUsage vm-stats)}
          {:service "jvm.memory.non-heap-usage" :metric (.nonHeapUsage vm-stats)}
          {:service "jvm.thread.daemon-count" :metric (.daemonThreadCount vm-stats)}
          {:service "jvm.thread.count" :metric (.threadCount vm-stats)}
          {:service "jvm.uptime" :metric (.uptime vm-stats)}
          {:service "jvm.fd-usage" :metric (.fileDescriptorUsage vm-stats)}]
         (for [[k v] (.memoryPoolUsage vm-stats)]
           {:service (str "jvm.memory.pool-usage." k) :metric v})
         (for [[k v] (.threadStatePercentages vm-stats)]
           {:service (str "jvm.thread.state." (.toLowerCase (str k))) :metric v})
         (for [[k v] (.garbageCollectors vm-stats)]
           {:service (str "jvm.gc." k ".time") :metric (.getTime v java.util.concurrent.TimeUnit/MILLISECONDS)})
         (for [[k v] (.garbageCollectors vm-stats)]
           {:service (str "jvm.gc." k ".runs") :metric (.getRuns v)}))]
    (map #(assoc % :service (str prefix (:service %)) :time epoch) metrics)))
