(ns bulk-loader.image-io
  (:gen-class :main true)
  (:require [clojure.java.io :refer [resource]]
            [neo4j-batch-inserter.core :refer [insert-batch]]
            [robert.hooke :as hooke]
            [taoensso.timbre :as timbre]
            [cheshire.core :refer [generate-string]])
  (:import (java.awt.image BufferedImage)
           (javax.imageio  ImageIO)
           (org.neo4j.unsafe.batchinsert BatchInserters
                                         BatchInserter)
           (org.neo4j.index.lucene.unsafe.batchinsert LuceneBatchInserterIndexProvider)))

(timbre/refer-timbre) ; Provides useful Timbre aliases in this ns

(defn get-pixels
  "Gets the pixels in a BufferedImage as a primitive byte[] array."
  (^bytes [^BufferedImage image width height]
          (let [raster (.getRaster image)]
            (.getDataElements raster 0 0 width height nil))))

(defn get-image-data
  "Gets all pixels from an image by URL."
  [resource]
  (let [image  (ImageIO/read resource)
        height (.getHeight image)
        width  (.getWidth image)]
    {:pixels (get-pixels image width height)
     :height height
     :width  width}))

(defn- create-node-map
  "Creates a node map from an id and a pixel."
  [id [b g r]]
  {:id (str id) :b b :g g :r r :type "pixel"})

(defn generate-node-maps
  "Given a pixel collection will return collection of node maps."
  [pixels]
  (map #(create-node-map %1 %2) (range) (partition 3 pixels)))

(defn- add-rel
  "Adds the specified relationship to the collection."
  [rels from to]
  (conj rels {:from {:id from :type "pixel"}
              :to   {:id to :type "pixel"}
              :type "neighbours"}))

(defn add-left-rel
  "Adds a relationship to the pixel immediately to the left pixel if applicable"
  [rels pixel-index width]
  (if (and (not= pixel-index 0)
           (not= (mod pixel-index width) 0))
    (add-rel rels pixel-index (dec pixel-index))
    rels))

(defn add-right-rel
  "Adds a relationship to the pixel immediately to the right if applicable"
  [rels pixel-index width pixel-count]
  (if (and (not= pixel-index (dec pixel-count))
           (not= (mod (inc pixel-index) width) 0))
    (add-rel rels pixel-index (inc pixel-index))
    rels))

(defn add-up-rel
  "Adds a relationship to the pixel immediatly above the pixel if applicable"
  [rels pixel-index width]
  (if (> pixel-index (dec width))
    (add-rel rels pixel-index (- pixel-index width))
    rels))

(defn add-down-rel
  "Adds a relationship to the pixel immediatly above the pixel if applicable"
  [rels pixel-index width pixel-count]
  (if (< (+ pixel-index width) pixel-count)
    (add-rel rels pixel-index (+ pixel-index width))
    rels))

(defn add-rels
  "Adds a map for each outgoing relationship."
  [rels pixel-index height width]
  (let [pixel-count (* width height)]
    (-> rels
      (add-left-rel pixel-index width)
      (add-up-rel pixel-index width)
      (add-right-rel pixel-index width pixel-count)
      (add-down-rel pixel-index width pixel-count))))

(defn generate-edge-maps
  "Given height and width will return a collection of maps representing all
  pixel relationships to its neighbours."
  [height width]
  (let [pixel-count (* height width)]
    (loop [pixel-index 0
           rels []]
      (if (< pixel-index pixel-count)
        (recur (inc pixel-index) (add-rels rels pixel-index height width))
        rels))))

(defn -main
  [& args]
  (let [{:keys [pixels width height]} (-> "krakow.JPG"
                                          resource
                                          get-image-data)]
    (info "Starting Neo batch insertion")
    (insert-batch
      "store" 
      {:auto-indexing {:red-fn :r :green-fn :g :blue-fn :b :id-fn :id}}
      {:nodes (generate-node-maps pixels)
       :relationships (generate-edge-maps width height)})))

; (timbre/refer-timbre) ; Provides useful Timbre aliases in this ns
; 
; (defn log-get-image-data
;   "logs the call to get image data"
;   [f resource]
;   (let [result (f resource)]
;     (info (generate-string {:message "image loaded from disk"  :path (.getPath resource) :width (:width result) :height (:height result)})) 
;     result))
;
; 
; (defn log-add-rels
;   "logs the call to get image data"
;   [f rels pixel-index height width]
;   (let [in-count (count rels)
;         result (f rels pixel-index height width)]
;     (debug (generate-string {:message "rel maps added for pixel" :pixel pixel-index :rels-added (- (count result) in-count)})) 
;     result))
; 
; (defn log-create-node-map
;   "Logs call to create-node-map"
;   [f id pixel]
;   (let [result (f id pixel)]
;     (debug (generate-string {:message "node map added for pixel" :map result})) ))
; 
; (hooke/add-hook #'get-image-data #'log-get-image-data)
; (hooke/add-hook #'add-rels #'log-add-rels)
; (hooke/add-hook #'create-node-map #'log-create-node-map)
