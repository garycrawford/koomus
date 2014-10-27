(ns bulk-loader.system
  (:require 
    [com.stuartsierra.component :as component]
    [bulk-loader.web-server :as web-server]
    [bulk-loader.redis :as redis]
    [environ.core :as environ]
    [metrics.jvm.core :refer  [instrument-jvm]]))

(import '(java.util.concurrent Future TimeUnit))

(require '[metrics.reporters.graphite :as graphite])
(import '[java.util.concurrent.TimeUnit])
(import '[com.codahale.metrics MetricFilter])

(def GR  
  (graphite/reporter  
    {:host "127.0.0.1"
     :prefix "koomus-metrics"
     :rate-unit TimeUnit/SECONDS
     :duration-unit TimeUnit/MILLISECONDS
     :filter MetricFilter/ALL}))

(graphite/start GR 10)

(require '[metrics.reporters.console :as console])

(def CRc  (console/reporter  {}))
(console/start CRc 10)

(require '[metrics.core :refer  [new-registry]])

(def reg  (new-registry))

(instrument-jvm)

(require '[metrics.counters :refer  [defcounter]])

(defcounter reg users-connected)

(require '[metrics.counters :refer  [inc!]])

(println "should havve run")

(def components [:web-server :redis])

(defrecord Bulk-Loader-System []
  component/Lifecycle
  (start [this]
    (component/start-system this components))
  (stop [this]
    (component/stop-system this components)))

(defn new-bulk-loader-system
  "Constructs a component system"
  []
  (map->Bulk-Loader-System
    {:web-server (web-server/new-web-server (environ/env :host) (environ/env :port))
     :redis (redis/new-redis (environ/env :redis-start-cmd) (environ/env :redis-conf-path))}))
