(ns bulk-loader.web-server
  (:require
    [com.stuartsierra.component :as component]
    [ring.middleware.json :as json-response]
    [ring.util.response :as util]
    [bulk-loader.queue :as blq]
    [scenic.routes :as scenic]
    [ring.adapter.jetty :as jetty]
    [ring.middleware.defaults :refer [wrap-defaults api-defaults]])
  (:import [java.lang.Integer]))

; (defn- dispatch
;   [path queue]
;   (blq/qpush queue path)
;   (util/response {:path path :status "submitted"}))

(def routes (scenic/load-routes-from-file "routes"))
(def base-handler (scenic/scenic-handler routes {:home (fn [req] (ring.util.response/response "home page"))
                                                 :add-image (fn [req] (ring.util.response/response "add image"))
                                                 :healthcheck (fn [req] (ring.util.response/response "healthcheck"))}))

(defn handler
  [queue]
  (-> base-handler 
      (json-response/wrap-json-response)
      (wrap-defaults api-defaults)))

(defrecord WebServer [queue]
  component/Lifecycle
  (start [this]
    (assoc this :server (jetty/run-jetty base-handler {:port 1234 :join? false}))
  ;   (if (and port host) 
  ;     (let [p (port-check port)]
  ;       (assoc this :server (jetty/run-jetty (handler queue) {:port p :join? false})))
  ;     this)
    
    )
  (stop [this]
    (.stop (:server this))
    this))

(defn new-web-server []
  (map->WebServer {}))
