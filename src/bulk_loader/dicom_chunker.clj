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

(defn get-first-slice-data
  [path]
  (let [mrg (io/get-pixels-for-slices path 0 2)]

    (for [x (range 12)
          y (range 12)
          z (range 2)
          :let [current (vector x y z)]]
      (let [neighbour-keys (potential-neighbour-keys current)
            deltas (map (fn [[k v]] (generate-delta mrg current v k)) neighbour-keys)
            linked (into {} (conj deltas (mrg current)))]
       (vector current linked)))))
