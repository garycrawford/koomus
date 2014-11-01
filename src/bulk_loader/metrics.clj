(ns bulk-loader.metrics
  (:require 
    [com.stuartsierra.component :as component]
    [environ.core :as environ]
    [metrics.jvm.core :refer [instrument-jvm]]
    [metrics.reporters.graphite :as graphite]
  )
  (:import 
    [java.util.concurrent TimeUnit]
    [com.codahale.metrics MetricFilter]
    )
  )

(def GR  
  (graphite/reporter  
    {:host "127.0.0.1"
     :prefix "koomus-metrics"
     :rate-unit TimeUnit/SECONDS
     :duration-unit TimeUnit/MILLISECONDS
     :filter MetricFilter/ALL}))


(defrecord Metrics []
  component/Lifecycle
  (start [this]
    (instrument-jvm)
    (graphite/start GR 10))
  (stop [this]
    (graphite/stop GR 10)))

