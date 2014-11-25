(ns bulk-loader.web-server
  (:use 
    [compojure.core])
  (:require
    [com.stuartsierra.component :as component]
    [bulk-loader.queue :as q]
    [compojure.route :as route]
    [ring.adapter.jetty :as jetty]
    [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))

(defn- dispatch
  [path queue]
  (q/qpush queue path)
  {:status 200 :body (str "<p>" path "</p>")})

(defn things
  [queue]
  (defroutes app-routes
    (context "/api" []
             ;; api/image?file-name=foo
             (GET "/image" {{path :path} :params} (dispatch path queue))
             (route/resources "/")
             (route/not-found "<h1>Not Found</h1>"))))

(defn handler
  [queue]
  (-> (things queue)
      (wrap-defaults api-defaults)))

(defrecord WebServer [host port queue] 
  component/Lifecycle
  (start [this]
    (assoc this :server (jetty/run-jetty (handler queue) {:port port :join? false})))
  (stop [this]
    (.stop (:server this))
    this))

(defn new-web-server [host port] 
  (map->WebServer {:host host :port port}))
