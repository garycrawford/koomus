(ns bulk-loader.dicom-chunker
  (:require [bulk-loader.dicom-io :as io]
            [clojure.algo.generic.functor :as algo]))

(defn- potential-neighbour-keys
  [[x y z]]
    {:+xΔ (vector (inc x) y z)
     :-xΔ (vector (dec x) y z)
     :+yΔ (vector x (inc y) z)
     :-yΔ (vector x (dec y) z)
     :+zΔ (vector x y (inc z))
     :-zΔ (vector x y (dec z))})

(defn- generate-delta
  [mrg source-id target-id label]
  (when-let [{tv :v} (mrg target-id)]
    (let [{sv :v} (mrg source-id)]
      {label (- sv tv)})))

(defn get-slice-data
  [path focus-index]
  (let [start-index (if (= focus-index 0) 0 (dec focus-index))
        slice-count (if (= focus-index 0) 2 3)
        mrg (io/get-pixels-for-slices path start-index slice-count)]
    (for [x (range 512)
          y (range 512)
          :let [current (vector x y focus-index)]]
      (let [neighbour-keys (potential-neighbour-keys current)
            deltas (map (fn [[k v]] (generate-delta mrg current v k)) neighbour-keys)
            linked (into {} (conj deltas (mrg current)))]
       (vector current linked)))))
