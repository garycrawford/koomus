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

(defrecord Metrics [host]
  component/Lifecycle
  (start [this]
    (assoc this :gr (graphite/reporter
                      {:host host
                      :prefix "koomus-metrics"
                      :rate-unit TimeUnit/SECONDS
                      :duration-unit TimeUnit/MILLISECONDS
                      :filter MetricFilter/ALL}))

    (instrument-jvm)
    (graphite/start (this :gr) 10))
  (stop [this]
    (graphite/stop (this :gr))))

(defn new-metrics  [host]
  (map->Metrics  {:host host}))
