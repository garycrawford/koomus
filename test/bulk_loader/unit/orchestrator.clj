(ns bulk-loader.unit.orchestrator
  (:use [midje.sweet :only (facts fact => just anything)])
  (:require [clojure.test :refer :all]
            [bulk-loader.extractor :as io]
            [bulk-loader.transformer :as tf]
            [bulk-loader.loader :as l]
            [bulk-loader.orchestrator :refer :all]))

(def _ anything)

(facts "constructing the web-server component"
  (fact "a web-server component should be generated"
        (let [result (new-orchestrator)]
          (keys result) => (just :queue))))

;; this var shadows a private funtion which needs to be tested
(def shadow-get-pixel-slices #'bulk-loader.orchestrator/get-slice-pixels)

(facts "pixel relationships should be calculated over a 3 layer sliding window"
  (fact "the first slice will only have one neighbour"
        (shadow-get-pixel-slices ..path.. 0) => ..result..
        (provided
          (io/get-pixels-for-slices ..path.. 0 2) => ..result..))
  
  (fact "the second slice will have two neighbours"
        (shadow-get-pixel-slices ..path.. 1) => ..result..
        (provided
          (io/get-pixels-for-slices ..path.. 0 3) => ..result..))

  (fact "other slices will have two neighbours"
        (shadow-get-pixel-slices ..path.. 10) => ..result..
        (provided
          (io/get-pixels-for-slices ..path.. 9 3) => ..result..)))

;; this var shadows a private funtion which needs to be tested
(def shadow-load-handler #'bulk-loader.orchestrator/load-handler)

(facts "the load handler coordinates ETL"
  (fact "functions will be called in extract -> transform -> load order"
        (shadow-load-handler ..path.. 0) => ..result..
        (provided
          (io/get-pixels-for-slices ..path.. 0 2) => ..pixels..
          (tf/generate-pixel-nodes ..pixels.. 0) => ..nodes..
          (l/send-msg ..nodes.. 0) => ..result..)))

(facts "pixels will be iterated over"
  (fact "each ETL function will be called 221 times"
        (load-dicom ..path.. 220) => _
        (provided
          (io/get-pixels-for-slices ..path.. _ _) => _ :times 220
          (tf/generate-pixel-nodes _ _) => _ :times 220
          (l/send-msg _ _) => _ :times 220)))
