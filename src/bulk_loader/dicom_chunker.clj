(ns bulk-loader.dicom-chunker
  (:require [bulk-loader.dicom-io :as io]))

(def dir "/opt/dev/koomus/resources/IMG00000")

(defn generate-keys
  [[x y z]]
  (vector
    (vector (inc x) y z)
    (vector (dec x) y z)
    (vector x (inc y) z)
    (vector x (dec y) z)
    (vector x (inc y) z)
    (vector x (dec y) z)
    

    ))

(defn get-first-slice-data
  [path]
  (let [[[_ slice-path-1] [_ slice-path-2]] (io/get-slices path 0 2)
        one (io/build-pixels slice-path-1 0)
        two (io/build-pixels slice-path-2 1)]

    (doseq [x (range 512)
            y (range 512)]
      (let [k (vector x y 0)]
        (when ) 
        )
      )
    ))




(client/post "http://127.0.0.1:8085/api/voxel" {:form-params {:a "b"} 
                                                :content-type :json})

;; this is how Hippo will consume the values sent to it (i.e. `(apply hash-map request-param)`)
(def id [0 0 0])
(def p-val {:v 123 :-xΔ 123 :+xΔ 13 :+yΔ 30 :-yΔ 147 :+zΔ 21 :-zΔ 23})

(->> [id p-val]
    generate-string
    parse-string
    (apply hash-map))
