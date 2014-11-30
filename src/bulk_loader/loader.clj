(ns bulk-loader.loader
  (:require 
    [clojure.tools.logging :as log]))

(defn send-msg
  [data slice-id]
  (log/info (str "sending: " (count data)))
  (count data))
