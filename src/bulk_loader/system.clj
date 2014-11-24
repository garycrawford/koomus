(ns bulk-loader.system
  (:require 
    [com.stuartsierra.component :as component]
    [bulk-loader.web-server :as web-server]
    [koomus.trees.metrics :as metrics]
    [environ.core :as environ]
  ))

(def components [:web-server :metrics])

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
     :metrics (metrics/new-metrics (environ/env :graphite-host))
    }))
