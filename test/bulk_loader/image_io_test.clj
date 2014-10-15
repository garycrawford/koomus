(ns bulk-loader.image-io-test
  (:use [midje.sweet :only (facts fact future-fact => contains)])
  (:require [clojure.test :refer :all]
            [bulk-loader.image-io :refer :all]
            [clojure.java.io :refer :all]))

(def uri_3x3_bw (resource "3x3_bw.JPG"))
(def uri_3x1_rgb (resource "3x1_rgb.JPG"))

(defn each-channel-of-type-byte?
  "Confirms that rgb values in a pixel are of type byte."
  [{:keys [b g r]}]
  (every? #(= java.lang.Byte (type %)) #{b g r}))

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

(facts "generate node maps"
  (fact "should contain one map entry for each pixel"
     (let [{:keys [pixels]} (get-image-data uri_3x1_rgb)]
       (count (generate-node-maps pixels)) => 3)
       (count (generate-node-maps (range 0 27))) => 9
       (count (generate-node-maps (range 0 2359296))) => 786432)

  (fact "should convert a byte[] into a collection of node maps"
    (let [{:keys [pixels]} (get-image-data uri_3x1_rgb)]
      (generate-node-maps pixels) => (contains
                                       {:b -1, :g 38, :r  0, :id "0" :type "pixel"}
                                       {:b  4, :g 50, :r -1, :id "1" :type "pixel"}
                                       {:b  0, :g -7, :r  0, :id "2" :type "pixel"})))

  (fact "will keep rgb byte values as type byte"
    (let [{:keys [pixels]} (get-image-data uri_3x1_rgb)]
      (first (generate-node-maps pixels)) => each-channel-of-type-byte?)))

(facts "left node relationships"
  (fact "should not be added for the first pixel in the image"
    (add-left-rel [] 0 100) => [])

  (fact "should not be added for the first pixel of a row"
    (add-left-rel [] 100 100) => []
    (add-left-rel [] 200 100) => [])

  (fact "should be added where a pixel exists to the left"
    (add-left-rel [] 1 100) => (contains {:from {:id 1 :type "pixel"}
                                          :to   {:id 0 :type "pixel"}
                                          :type "neighbours"})))

(facts "right node relationships"
  (fact "should not be added for the last pixel in the image"
    (add-right-rel [] 399 100 400) => [])

  (fact "should not be added for the last pixel of a row"
    (add-right-rel [] 199 100 400) => []
    (add-right-rel [] 299 100 400) => [])

  (fact "should be added where a pixel exists to the right"
    (add-right-rel [] 1 100 400) => (contains {:from {:id 1 :type "pixel"}
                                               :to   {:id 2 :type "pixel"}
                                               :type "neighbours"})))

(facts "up node relationships"
  (fact "should not be added for the first row of pixels in the image"
    (add-up-rel [] 1 100) => [])

  (fact "should be added for all pixels in rows after the first"
    (add-up-rel [] 399 100) => (contains {:from {:id 399 :type "pixel"}
                                          :to   {:id 299 :type "pixel"}
                                          :type "neighbours"})))

(facts "down node relationships"
  (fact "should not be added for the last row of pixels in the image"
    (add-down-rel [] 399 100 400) => [])

  (fact "should be added for all pixels in rows prior to the last row"
    (add-down-rel [] 101 100 400) => (contains {:from {:id 101 :type "pixel"}
                                                :to   {:id 201 :type "pixel"}
                                                :type "neighbours"
                                                })))
(facts "generate all edges with a height and width"
  (fact "should be converted into a collection of edge maps"
    (generate-edge-maps 1 2) =>
        (contains
          {:from {:id 0 :type "pixel"}
           :to {:id 1 :type "pixel"}
           :type "neighbours"}
          {:from {:id 1 :type "pixel"}
           :to {:id 0 :type "pixel"}
           :type "neighbours"}
          :in-any-order)

    (generate-edge-maps 1 3) =>
        (contains
          {:from {:id 0 :type "pixel"}
           :to {:id 1 :type "pixel"}
           :type "neighbours"}
          {:from {:id 1 :type "pixel"}
           :to {:id 0 :type "pixel"}
           :type "neighbours"}
          {:from {:id 1 :type "pixel"}
           :to {:id 2 :type "pixel"}
           :type "neighbours"} 
          {:from {:id 2 :type "pixel"}
           :to {:id 1 :type "pixel"}
           :type "neighbours"}
          :in-any-order)

    (count (generate-edge-maps 3 3)) => 24
    (generate-edge-maps 3 3) =>
        (contains
          {:from {:id 0 :type "pixel"}
           :to {:id 1 :type "pixel"}
           :type "neighbours"}
          {:from {:id 1 :type "pixel"}
           :to {:id 0 :type "pixel"}
           :type "neighbours"}
          {:from {:id 0 :type "pixel"}
           :to {:id 3 :type "pixel"}
           :type "neighbours"}
          {:from {:id 3 :type "pixel"}
           :to {:id 0 :type "pixel"}
           :type "neighbours"}
          {:from {:id 1 :type "pixel"}
           :to {:id 2 :type "pixel"}
           :type "neighbours"}
          {:from {:id 2 :type "pixel"}
           :to {:id 1 :type "pixel"}
           :type "neighbours"}
          {:from {:id 1 :type "pixel"}
           :to {:id 4 :type "pixel"}
           :type "neighbours"}
          {:from {:id 4 :type "pixel"}
           :to {:id 1 :type "pixel"}
           :type "neighbours"}
          {:from {:id 2 :type "pixel"}
           :to {:id 5 :type "pixel"}
           :type "neighbours"}
          {:from {:id 5 :type "pixel"}
           :to {:id 2 :type "pixel"}
           :type "neighbours"}
          {:from {:id 3 :type "pixel"}
           :to {:id 4 :type "pixel"}
           :type "neighbours"}
          {:from {:id 4 :type "pixel"}
           :to {:id 3 :type "pixel"}
           :type "neighbours"}
          {:from {:id 3 :type "pixel"}
           :to {:id 6 :type "pixel"}
           :type "neighbours"}
          {:from {:id 6 :type "pixel"}
           :to {:id 3 :type "pixel"}
           :type "neighbours"}
          {:from {:id 4 :type "pixel"}
           :to {:id 5 :type "pixel"}
           :type "neighbours"}
          {:from {:id 5 :type "pixel"}
           :to {:id 4 :type "pixel"}
           :type "neighbours"}
          {:from {:id 4 :type "pixel"}
           :to {:id 7 :type "pixel"}
           :type "neighbours"}
          {:from {:id 7 :type "pixel"}
           :to {:id 4 :type "pixel"}
           :type "neighbours"}
          {:from {:id 5 :type "pixel"}
           :to {:id 8 :type "pixel"}
           :type "neighbours"}
          {:from {:id 8 :type "pixel"}
           :to {:id 5 :type "pixel"}
           :type "neighbours"}
          {:from {:id 6 :type "pixel"}
           :to {:id 7 :type "pixel"}
           :type "neighbours"}
          {:from {:id 7 :type "pixel"}
           :to {:id 6 :type "pixel"}
           :type "neighbours"}
          {:from {:id 7 :type "pixel"}
           :to {:id 8 :type "pixel"}
           :type "neighbours"}
          {:from {:id 8 :type "pixel"}
           :to {:id 7 :type "pixel"}
           :type "neighbours"}
          :in-any-order)))
