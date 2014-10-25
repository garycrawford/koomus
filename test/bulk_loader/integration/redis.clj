(ns bulk-loader.integration.redis
  (:use [midje.sweet :only (facts fact future-fact => contains throws)])
  (:require [com.stuartsierra.component :as component]
            [clojure.test :refer :all]
            [bulk-loader.redis :refer [new-redis]]
            [taoensso.carmine :as car :refer (wcar shutdown)]))

(def ^{:private true} server-conn {:pool :none :spec {}}) ; See `wcar` docstring for opts
(defmacro tcar* [& body] `(car/wcar server-conn ~@body))

(def redis (new-redis "redis-server" "/etc/redis/"))

(fact :local-only "Calling start will bring up Redis and stop will shutdown"
  (tcar* (car/ping)) => (throws Exception "Carmine connection error")
  (alter-var-root #'redis component/start)
  (tcar* (car/ping)) => "PONG"
  (alter-var-root #'redis component/stop)
  (tcar* (car/ping)) => (throws Exception "Carmine connection error"))
