(ns bulk-loader.zygote
  (:require 
    [bulk-loader.system :as system]
    [com.stuartsierra.component :as component])
  (:use
    clojure.tools.logging
    clj-logging-config.log4j)
  (:gen-class :main true))

(set-logger!)

(def bulk-loader-system (system/new-bulk-loader-system))

(defn app-init
  []
  (alter-var-root #'bulk-loader-system component/start))

(defn -main []
  (app-init))
