(ns bulk-loader.web-server
  (:use 
    [compojure.core])
  (:require
    [com.stuartsierra.component :as component]
    [compojure.route :as route]
    [ring.middleware.json :as middleware]
    [ring.adapter.jetty :as jetty]
    [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
    [bulk-loader.image-io :refer [insert-image]]))

(defroutes app-routes
  (context "/api" []
    (GET "/image" {{file-name :file-name} :params} (insert-image file-name "store")) 
  (route/resources "/")
  (route/not-found "<h1>Not Found</h1>")))

(defn handler
  []
  (-> app-routes
      (wrap-defaults api-defaults)))

(defrecord WebServer [host port] 
  component/Lifecycle
  (start [this]
    (assoc this :server (jetty/run-jetty (handler) {:port port :join? false})))
  (stop [this]
    (.stop (:server this))
    this))

(defn new-web-server [host port] 
  (map->WebServer {:host host :port port}))
