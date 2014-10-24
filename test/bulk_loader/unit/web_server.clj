(ns bulk-loader.unit.web-server
  (:use [midje.sweet :only (facts fact => just)])
  (:require [clojure.test :refer :all]
            [ring.adapter.jetty :as jetty]
            [bulk-loader.web-server :refer :all]))

(facts "constructing the web-server component"
  (fact "a web-server component should be generated"
        (new-web-server "localhost" 1234) => (just [:port 1234]
                                                   [:host "localhost"])))
