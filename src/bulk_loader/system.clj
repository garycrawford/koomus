(ns bulk-loader.system
  (:require 
    [com.stuartsierra.component :as component]
    [bulk-loader.web-server :as web-server]
    [bulk-loader.redis :as redis]
    [bulk-loader.metrics]
    [environ.core :as environ]
  )
  (:import [bulk-loader.metrics Metrics])
  )

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
    {
     :web-server (web-server/new-web-server (environ/env :host) (environ/env :port))
     :redis (redis/new-redis (environ/env :redis-start-cmd) (environ/env :redis-conf-path))
    }))
