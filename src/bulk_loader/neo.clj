(ns bulk-loader.neo
  (:require [clojure.java.io :refer [resource]]
            [com.stuartsierra.component :as component]
            [neo4j-batch-inserter.core :refer [insert-batch]]
            [taoensso.timbre :as timbre]
            [bulk-loader.image-io :as io])
  (:import (org.neo4j.unsafe.batchinsert BatchInserters
                                         BatchInserter)
           (org.neo4j.index.lucene.unsafe.batchinsert LuceneBatchInserterIndexProvider)))

(timbre/refer-timbre) ; Provides useful Timbre aliases in this ns

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

(defn insert-image
  [in-path out-path]
  (let [{:keys [pixels width height]} (-> in-path
                                          resource
                                          io/get-image-data)]
    (info "Starting Neo batch insertion")
    (insert-batch
      out-path
      {:auto-indexing {:red-fn :r :green-fn :g :blue-fn :b :id-fn :id}}
      {:nodes (generate-node-maps pixels)
       :relationships (generate-edge-maps width height)})))
