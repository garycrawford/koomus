(ns bulk-loader.metrics
  (:require 
    [com.stuartsierra.component :as component]
    [metrics.jvm.core :refer [instrument-jvm]]
    [metrics.reporters.graphite :as graphite]
    [metrics.core :refer [new-registry]])
  (:import 
    [java.util.concurrent TimeUnit]
    [com.codahale.metrics MetricFilter]))

(defn- generate-reporter
  [reg host]
  (graphite/reporter reg {:host host
                          :prefix "koomus-metrics"
                          :rate-unit TimeUnit/SECONDS
                          :duration-unit TimeUnit/MILLISECONDS
                          :filter MetricFilter/ALL}))

(defn- init-reporter
  [{:keys [host]}]
  (let [reg (new-registry)]
        (instrument-jvm reg)
        (let [reporter (generate-reporter reg host)]
          (graphite/start reporter 10)
          reporter)))

(defn- start
  [{:keys [gr] :as this}]
  (if gr
      (do (graphite/start gr 10) this)
      (->> this init-reporter (assoc this :gr))))

(defn- stop
  [{:keys [gr] :as this}]
  (graphite/stop gr)
  this)

(defrecord Metrics [host]
  component/Lifecycle
  (start [this]
    (start this))
  (stop [this]
    (stop this)))

(defn new-metrics  [host]
  (map->Metrics {:host host}))
