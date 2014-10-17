(ns bulk-loader.system-test
  (:use [midje.sweet :only (facts fact contains => anything)])
  (:require [clojure.test :refer :all]
            [bulk-loader.system :refer :all]
            [bulk-loader.web-server :refer :all]))

(facts "constructing the component system"
  (fact "a system map should be generated"
       (new-bulk-loader-system) => (contains {:web-server anything})))
