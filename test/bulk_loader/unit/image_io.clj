(ns bulk-loader.unit.image-io
  (:use [midje.sweet :only (facts fact future-fact => contains)])
  (:require [clojure.test :refer :all]
            [bulk-loader.image-io :refer :all]
            [clojure.java.io :refer :all]))

(def uri_3x3_bw (resource "3x3_bw.JPG"))
(def uri_3x1_rgb (resource "3x1_rgb.JPG"))

(facts "when loading an image"
  (fact "given a URL to a valid JPG image data should be returned"
    (let [{:keys [pixels width height]} (get-image-data uri_3x1_rgb)]
      (count pixels) => 9
      width          => 3
      height         => 1)

    (let [{:keys [pixels width height]} (get-image-data uri_3x3_bw)]
      (count pixels) => 27
      width          => 3
      height         => 3)))
