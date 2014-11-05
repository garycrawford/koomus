(ns bulk-loader.metrics
  (:require 
    [com.stuartsierra.component :as component]
    [environ.core :as environ]
    [metrics.jvm.core :refer [instrument-jvm]]
    [metrics.reporters.graphite :as graphite]
    [metrics.core :refer [new-registry]]
  )
  (:import 
    [java.util.concurrent TimeUnit]
    [com.codahale.metrics MetricFilter]
    )
  )

(defrecord Metrics [host]
  component/Lifecycle
  (start [this]
    (when-not (contains? this :gr)
      (def reg (new-registry))
      (instrument-jvm reg)
      (let [reporter (graphite/reporter reg
                      {:host host
                      :prefix "koomus-metrics"
                      :rate-unit TimeUnit/SECONDS
                      :duration-unit TimeUnit/MILLISECONDS
                      :filter MetricFilter/ALL})]
        (graphite/start reporter 10) 
        (assoc this :gr reporter)))
    )
  (stop [this]
    (graphite/stop (:gr this))
    this
    ))

(defn new-metrics  [host]
  (map->Metrics  {:host host}))
