(ns bulk-loader.orchestrator
  (:require [com.stuartsierra.component :as component]
            [bulk-loader.extract :as io]
            [bulk-loader.transform :as tf]
            [bulk-loader.load :as l]
            [bulk-loader.queue :as q]))

(defn- get-slice-pixels
  [path slice-id]
  (let [start-index (if (= slice-id 0) 0 (dec slice-id))
        slice-count (if (= slice-id 0) 2 3)]
    (io/get-pixels-for-slices path start-index slice-count)))

(defn- load-handler
  [path slice-id]
  (-> path
      (get-slice-pixels slice-id)
      (tf/generate-pixel-nodes slice-id)
      (l/send-msg slice-id)))

(defn load-dicom
  [path slices]
  (let [work (partition-all 55 (range slices))]
    (pmap (fn [workload]
            (doall
              (map
                (fn [slice-id] (load-handler path slice-id))
                workload)))
          work)))

(defrecord Orchestrator [queue] 
  component/Lifecycle
  (start [this]
    (q/register queue load-dicom)
    this)
  (stop [this]
    this))

(defn new-orchestrator []
  (map->Orchestrator {}))
