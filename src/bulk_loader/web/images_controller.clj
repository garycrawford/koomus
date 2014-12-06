(ns bulk-loader.web.images-controller
  (:require
    [com.stuartsierra.component :as component]
    [ring.util.response :as util]
    [bulk-loader.infra.queue :as blq]))

(defn- dispatch
  [path queue]
  (blq/qpush queue path)
  (util/response {:path path :status "submitted"}))

(defn add-image
  [{queue :queue}]
  (fn [{{path :path} :params}]
    (dispatch path queue)))

(defrecord ImagesController [queue]
  component/Lifecycle
  (start [this]
    this)
  (stop [this]
    this))

(defn new-images-controller []
  (map->ImagesController {}))
