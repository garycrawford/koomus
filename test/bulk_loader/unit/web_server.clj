(ns bulk-loader.unit.web-server
  (:use [midje.sweet :only (facts fact => just)])
  (:require [clojure.test :refer :all]
            [bulk-loader.web-server :refer :all]))

(facts "constructing the web-server component"
  (fact "a web-server component should be generated"
        (let [result (new-web-server)]
          (keys result) => (just :handler))))
