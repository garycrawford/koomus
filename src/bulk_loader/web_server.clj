(ns bulk-loader.web-server
  (:use 
    [compojure.core])
  (:require
    [com.stuartsierra.component :as component]
    [compojure.route :as route]
    [ring.adapter.jetty :as jetty]
    [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))

(defroutes app-routes
  (context "/api" []
    ;; api/image?file-name=foo       
    (GET "/image" {{file-name :file-name} :params} {:status 200 :body (str "<p>" file-name "</p>")}) 
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
