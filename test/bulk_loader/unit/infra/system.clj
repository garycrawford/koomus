(ns bulk-loader.unit.infra.system
  (:use [midje.sweet :only (facts fact => just)])
  (:require [clojure.test :refer :all]
            [bulk-loader.infra.system :refer :all]))

(facts "constructing the component system"
  (fact "a system component should be generated"
        (let [result (new-bulk-loader-system)]
          (keys result) => (just :web-server :routes :handler :metrics :queue :orchestrator :logger :images-controller :in-any-order))))
