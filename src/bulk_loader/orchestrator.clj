(ns bulk-loader.orchestrator
  (:require [bulk-loader.extract :as io]
            [bulk-loader.transform :as tf]
            [bulk-loader.load :as l]))

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
  [path]
  (let [work (partition-all 55 (range 221))]
    (pmap (fn [workload]
            (doall
              (map
                (fn [slice-id] (load-handler path slice-id))
                workload)))
          work)))
