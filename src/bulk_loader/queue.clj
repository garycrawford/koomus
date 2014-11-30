(ns bulk-loader.queue
  (:require 
    [com.stuartsierra.component :as component]))

(defn- qpop
  [{m :memory}]
  (let [v (peek @m)]
    (swap! m pop)
    v))

(defn qpush
  [{h :handler m :memory :as this} msg]
  (swap! m conj msg) 
  (.start (Thread. (fn [] (@h (qpop this) 220)))))

(defn register
  [{m :handler} handler-fn]
  (reset! m handler-fn))

(defrecord Queue [] 
  component/Lifecycle
  (start [this]
    (assoc this :memory (atom (list)) :handler (atom nil)))
  (stop [this]
    (dissoc this :memory)))

(defn new-queue []
  (->Queue))
