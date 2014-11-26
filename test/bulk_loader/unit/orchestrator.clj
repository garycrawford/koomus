(ns bulk-loader.unit.orchestrator
  (:use [midje.sweet :only (facts fact => just anything)])
  (:require [clojure.test :refer :all]
            [bulk-loader.extract :as io]
            [bulk-loader.transform :as tf]
            [bulk-loader.load :as l]
            [bulk-loader.orchestrator :refer :all]))

(def _ anything)

(facts "constructing the web-server component"
  (fact "a web-server component should be generated"
        (let [result (new-orchestrator)]
          (keys result) => (just :queue))))

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

(def shadow-load-handler #'bulk-loader.orchestrator/load-handler)

(facts "the load handler coordinated ETL"
  (fact "order of call will be extract -> transform -> load"
        (shadow-load-handler ..path.. 0) => ..result..
        (provided
          (io/get-pixels-for-slices ..path.. 0 2) => ..pixels..
          (tf/generate-pixel-nodes ..pixels.. 0) => ..nodes..
          (l/send-msg ..nodes.. 0) => ..result..)))

(facts "pixels will be iterated over"
  (fact "each ETL function will be called 221 times"
        (load-dicom ..path..) => _
        (provided
          (io/get-pixels-for-slices ..path.. _ _) => _ :times 221
          (tf/generate-pixel-nodes _ _) => _ :times 221
          (l/send-msg _ _) => _ :times 221)))
