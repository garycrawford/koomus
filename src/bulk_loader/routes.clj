(ns bulk-loader.routes
  (:require
    [com.stuartsierra.component :as component]
    [ring.util.response :as util]
    [bulk-loader.images-controller :as images]
    [scenic.routes :as scenic]))

(defn routes-map
  [images-controller] 
  {:home (fn [req] (util/response "home page"))
   :add-image (images/add-image images-controller)
   :healthcheck (fn [req] (util/response "healthcheck"))})

(defrecord Routes [images-controller]
  component/Lifecycle
  (start [this]
    (assoc this :routes (scenic/load-routes-from-file "routes")
                :routes-map (routes-map images-controller)))
  (stop [this]
    (dissoc this :routes)))

(defn new-routes []
  (map->Routes {}))
