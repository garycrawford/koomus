(ns bulk-loader.web.web-server
  (:require
    [com.stuartsierra.component :as component]
    [ring.adapter.jetty :as jetty]))

(defrecord WebServer [handler]
  component/Lifecycle
  (start [this]
    (assoc this :server (jetty/run-jetty (:handler handler) {:port 1234 :join? false})))
  (stop [this]
    (.stop (:server this))
    (dissoc this :server)))

(defn new-web-server []
  (map->WebServer {}))
