(ns bulk-loader.web.handler
  (:require
    [com.stuartsierra.component :as component]
    [ring.middleware.json :as json-response]
    [scenic.routes :as scenic]
    [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))

(defn create-handler
  [{:keys [routes routes-map]}]
  (-> (scenic/scenic-handler routes routes-map)
      (json-response/wrap-json-response)
      (wrap-defaults api-defaults)))

(defrecord Handler [routes]
  component/Lifecycle
  (start [this]
    (assoc this :handler (create-handler routes)))
  (stop [this]
    (dissoc this :handler)))

(defn new-handler []
  (map->Handler {}))
