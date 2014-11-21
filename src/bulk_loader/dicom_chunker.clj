(ns bulk-loader.dicom-chunker
  (:require [bulk-loader.dicom-io :as io]
            [clojure.algo.generic.functor :as algo]))

(def dir "/Users/gcrawfor/Projects/koomus-ops/dev/koomus/resources/IMG00000")

(defn potential-neighbour-keys
  [[x y z]]
  (merge
    {:+xΔ (vector (inc x) y z)}
    {:-xΔ (vector (dec x) y z)}
    {:+yΔ (vector x (inc y) z)}
    {:-yΔ (vector x (dec y) z)}
    {:+zΔ (vector x y (inc z))}
    {:-zΔ (vector x y (dec z))}))

(defn- upgrade-pixel
  [mrg source-id target-id label]
  (when-let [{tv :v :as target} (mrg target-id)]
    (let [{sv :v :as source} (mrg source-id)]
      (assoc source label (- sv tv)))))

(defn get-first-slice-data
  [path]
  (let [[[_ slice-path-1] [_ slice-path-2]] (io/get-slices path 0 2)
        one (io/build-pixels slice-path-1 0)
        two (io/build-pixels slice-path-2 1)
        mrg (merge one two)]

    (for [x (range 20)
          y (range 20)
          z (range 1)
          :let [current (vector x y z)]]
      (let [neighbour-keys (potential-neighbour-keys current)
            linked (map (fn [[k v]] (upgrade-pixel mrg current v k)) neighbour-keys)]
         (filter (complement nil?) linked)))))




; (client/post "http://127.0.0.1:8085/api/voxel" {:form-params {:a "b"} 
;                                                 :content-type :json})

;; this is how Hippo will consume the values sent to it (i.e. `(apply hash-map request-param)`)
(def id [0 0 0])
(def p-val {:v 123 :-xΔ 123 :+xΔ 13 :+yΔ 30 :-yΔ 147 :+zΔ 21 :-zΔ 23})
