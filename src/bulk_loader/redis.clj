(ns bulk-loader.redis
    (:require
      [com.stuartsierra.component :as component]
      [clj-commons-exec :as exec]
      [taoensso.carmine :as car :refer (wcar shutdown)])
    (:import                   
      [java.io ByteArrayOutputStream]))
    
(defn- stream-contains-redis-started-msg?
  "Checks Redis output stream for the Redis started sub-string"
  [redis-out]
  (let [msg (String. (.toByteArray redis-out))]
    (.contains msg "The server is now ready to accept connections on port")))

(defn- redis-ready?
  "Blocks until Redis is ready to receive connections"
  [redis-out]
  (when-not (stream-contains-redis-started-msg? redis-out)
    (recur redis-out)))

(defn start-redis
  "Starts Redis"
  [start-command conf-path]
  (let [redis-out (ByteArrayOutputStream.)]
    (exec/sh
      [start-command conf-path] 
      {:shutdown true :out redis-out :close-out? true})
    (redis-ready? redis-out)))

(def ^{:private true} server-conn {:pool :none :spec {}}) ; See `wcar` docstring for opts
(defmacro wcar* [& body] `(car/wcar server-conn ~@body))

(defn stop-redis
  "Stops Redis"
  []
  (try
    (wcar* (car/shutdown))
    (catch Exception e (.getMessage e))))

(defrecord Redis
  [redis-start-command redis-conf-path execute-fn]
  component/Lifecycle
  (start [this]
    (start-redis redis-start-command redis-conf-path)
    this)
  (stop [this]
    (stop-redis)
    this))

(defn new-redis
  "Constructs a new Redis component"
  [redis-start-command redis-conf-path]
  (map->Redis {:redis-start-command redis-start-command 
               :redis-conf-path redis-conf-path}))
