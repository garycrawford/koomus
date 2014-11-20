(ns bulk-loader.system
  (:require 
    [com.stuartsierra.component :as component]
    [bulk-loader.web-server :as web-server]
    [bulk-loader.redis :as redis]
    [koomus.trees.metrics :as metrics]
    [environ.core :as environ]
  ))

(def components [:web-server :redis :metrics])

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
    {
     :web-server (web-server/new-web-server (environ/env :host) (environ/env :port))
     :redis (redis/new-redis (environ/env :redis-start-cmd) (environ/env :redis-conf-path))
     :metrics (metrics/new-metrics (environ/env :graphite-host))
    }))
