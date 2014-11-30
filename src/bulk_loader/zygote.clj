(ns bulk-loader.zygote
  (:require 
    [bulk-loader.system :as system]
    [com.stuartsierra.component :as component])
  (:gen-class :main true))

(def bulk-loader-system (system/new-bulk-loader-system))

(defn app-init
  []
  (alter-var-root #'bulk-loader-system component/start))

(defn -main []
  (app-init))
