(ns bulk-loader.transform)

(defn- potential-neighbour-keys
  [current]
  (let [x ^long (current 0)
        y ^long (current 1)
        z ^long (current 2)]
    {:+xΔ (vector (inc x) y z)
     :-xΔ (vector (dec x) y z)
     :+yΔ (vector x (inc y) z)
     :-yΔ (vector x (dec y) z)
     :+zΔ (vector x y (inc z))
     :-zΔ (vector x y (dec z))}))

(defn- generate-delta
  [pixels ^long current-val target-id label]
  (when-let [target (pixels target-id)]
    (let [target-val ^long (target :v)]
      (hash-map label (- current-val target-val)))))

(defn generate-pixel-nodes
  [pixels slice-index]
    (for [x (range 512)
          y (range 512)
          :let [current-id (vector x y slice-index)
                current-pixel (pixels current-id)
                neighbour-keys (potential-neighbour-keys current-id)
                deltas (map #(generate-delta pixels (current-pixel :v) (val %) (key %)) neighbour-keys)
                linked (into {} (conj deltas current-pixel))]]
       (vector current-id linked)))
