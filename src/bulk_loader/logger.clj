(ns bulk-loader.logger
  (:require 
    [com.stuartsierra.component :as component]
    [environ.core :as environ]
    [clj-logging-config.log4j :as log-config]
    [clojure.tools.logging :as log]
    [bulk-loader.extractor :as extractor]
    [bulk-loader.loader :as loader]
    [robert.hooke :as hooke]
    [cheshire.core :as json]))

(def app-name {:appname "bulk-loader"})
(def uncached (merge {:action "uncached-access"} app-name))

(defn- log-uncached-access
  [f name args]
  (let [msg (merge uncached {:msg name})
        json-msg (json/generate-string msg)]
    (log/debug json-msg)
    (apply f args)))

(defn- log-uncached-build-pixels
  [f & args]
  (log-uncached-access f "bulk-loader.extractor/build-pixels" args))

(defn- log-uncached-order-files-by-slice
  [f & args]
  (log-uncached-access f "bulk-loader.extractor/order-files-by-slice" args))

(defn- start-logger
  []
  (log-config/set-logger!)
  (hooke/add-hook #'extractor/order-files-by-slice #'log-uncached-order-files-by-slice)
  (hooke/add-hook #'extractor/build-pixels #'log-uncached-build-pixels)
;  (hooke/add-hook #'loader/send-msg #'log-send-msg)
  )

(defrecord Logger []
  component/Lifecycle
  (start [this]
    (start-logger)
    this)
  (stop [this]
    this))

(defn new-logger []
  (map->Logger {}))
