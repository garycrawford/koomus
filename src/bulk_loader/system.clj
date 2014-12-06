(ns bulk-loader.system
  (:require 
    [com.stuartsierra.component :as component]
    [bulk-loader.web-server :as web-server]
    [bulk-loader.queue :as queue]
    [bulk-loader.routes :as routes]
    [bulk-loader.orchestrator :as orchestrator]
    [bulk-loader.logger :as logger]
    [bulk-loader.handler :as handler]
    [bulk-loader.images-controller :as images]
    [koomus.trees.metrics :as metrics]
    [environ.core :as environ]))

(def components [:web-server :handler :routes :metrics :queue :orchestrator :logger :images-controller])

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
    {:routes (component/using
               (routes/new-routes)
               {:images-controller :images-controller})
     :handler (component/using
                   (handler/new-handler)
                   {:routes :routes})
     :web-server (component/using
                   (web-server/new-web-server)
                   {:handler :handler})
     :metrics (metrics/new-metrics (environ/env :graphite-host))
     :queue (queue/new-queue)
     :orchestrator (component/using
                     (orchestrator/new-orchestrator)
                     {:queue :queue})
     :logger (logger/new-logger)
     :images-controller (component/using
                          (images/new-images-controller)
                          {:queue :queue})}))
