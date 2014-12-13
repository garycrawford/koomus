(ns bulk-loader.deployment.utils
  (:require [stencil.core :as stencil]))

(defn -main
  [& args]
  (let [version (System/getProperty "bulk-loader.version")
        dockerfile (stencil/render-file "Dockerfile.mustache" {:version version})]
    (spit "Dockerfile" dockerfile)
    (System/exit 0)))
