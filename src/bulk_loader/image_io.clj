(ns bulk-loader.image-io
  (:require [taoensso.timbre :as timbre])
  (:import (java.awt.image BufferedImage)
           (javax.imageio  ImageIO)))

(timbre/refer-timbre) ; Provides useful Timbre aliases in this ns

(defn- get-pixels
  "Gets the pixels in a BufferedImage as a primitive byte[] array."
  (^bytes [^BufferedImage image width height]
          (let [raster (.getRaster image)]
            (.getDataElements raster 0 0 width height nil))))

(defn get-image-data
  "Gets all pixels from an image by URL."
  [resource]
  (let [image  (ImageIO/read resource)
        height (.getHeight image)
        width  (.getWidth image)]
    {:pixels (get-pixels image width height)
     :height height
     :width  width}))
