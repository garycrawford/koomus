(ns bulk-loader.unit.system
  (:use [midje.sweet :only (facts fact => just)])
  (:require [clojure.test :refer :all]
            [bulk-loader.system :refer :all]))

(facts "constructing the component system"
  (fact "a system component should be generated"
        (let [result (new-bulk-loader-system)]
          (keys result) => (just :web-server :metrics :queue :orchestrator :in-any-order))))
