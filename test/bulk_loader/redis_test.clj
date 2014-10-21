(ns bulk-loader.image-io-test
  (:use [midje.sweet :only (facts fact future-fact => contains)])
  (:require [clojure.test :refer :all]
            [bulk-loader.redis :refer :all]))
