(ns bulk-loader.zygote
  (:require 
    [com.stuartsierra.component :as component] 
    ; [bulk-loader.system :as system]
    
    )
 ; (:gen-class)
  
  )

;(def bulk-loader-system (system/new-bulk-loader-system))

(defn app-init
  []
 ; (alter-var-root #'bulk-loader-system component/start)
  
  )

(defn -main []
  (app-init))
