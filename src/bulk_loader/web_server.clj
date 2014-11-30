(ns bulk-loader.web-server
  (:use 
    [compojure.core])
  (:require
    [com.stuartsierra.component :as component]
    [bulk-loader.queue :as blq]
    [compojure.route :as route]
    [ring.adapter.jetty :as jetty]
    [ring.middleware.defaults :refer [wrap-defaults api-defaults]])
  (:import [java.lang.Integer]))

(defn- dispatch
  [path queue]
  (blq/qpush queue path)
  {:status 200 :body (str "<p>" path "</p>")})

(defn generate-routes
  [queue]
  (defroutes app-routes
    (context "/api" []
             ;; api/image?file-name=foo
             (GET "/image" {{path :path} :params} (dispatch path queue))
             (GET "/healthcheck" [] {:status 200 :body "<p>alive!</p>"})
             (route/resources "/")
             (route/not-found "<h1>Not Found</h1>"))))

(defn handler
  [queue]
  (-> (generate-routes queue)
      (wrap-defaults api-defaults)))

(defrecord WebServer [queue host port]
  component/Lifecycle
  (start [this]
    (if (and port host) 
      (let [p (Integer/parseInt port)]
        (assoc this :server (jetty/run-jetty (handler queue) {:port p :join? false})))
      this))
  (stop [this]
    (.stop (:server this))
    this))

(defn new-web-server [host port]
  (map->WebServer {:host host :port port}))
