(ns #^{:doc "Manipulate DICOM files"
       :author "Kevin A. Archie <karchie@wustl.edu>"}
  bulk-loader.dicom-io
  (:import (java.io BufferedInputStream
		    File
		    FileInputStream
		    InputStream
		    IOException)
           (java.util.concurrent Executors)
	   (java.util.zip GZIPInputStream)
	   (org.dcm4che2.data DicomObject
			      Tag
			      UID
                              VR)
	   (org.dcm4che2.io DicomInputStream
			    DicomOutputStream
			    StopTagInputHandler)
           (org.dcm4che2.net Device
                             NetworkApplicationEntity
                             NetworkConnection
                             TransferCapability))
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.set]
            [cheshire.core :refer [generate-string parse-string]]
            [clj-http.client :as client])
  (:use clojure.test))

(def
  #^{:private true
     :doc "Files with this suffix are assumed to be gzip compressed."}
  gzip-suffix ".gz")

(defn- make-caused-IOException
  "Creates an IOException with the given cause."
  ([cause]
     (doto (IOException.)
       (.initCause cause)))
  ([cause message]
     (doto (IOException. message)
       (.initCause cause))))

(deftest make-caused-IOException-test
  (let [cause (Exception.)]
    (is (nil? (.getCause (IOException.))))
    (is (= cause
	   (.getCause (make-caused-IOException cause))))
    (is (= cause
	   (.getCause (make-caused-IOException cause "oh no!"))))))

(defn read-stream
  "Reads a DICOM object from an InputStream."
  ([in-s max-tag]
     (io!
      (with-open
          [buf-in-s (BufferedInputStream. in-s)
           dicom-in-s (DicomInputStream. buf-in-s)]
        (when max-tag
          (.setHandler dicom-in-s
                       (StopTagInputHandler.
                        (inc (max max-tag Tag/SOPClassUID)))))
        (try
          (let [obj (.readDicomObject dicom-in-s)]
            (when-not (or (.contains obj Tag/FileMetaInformationVersion)
                          (.contains obj Tag/SOPClassUID))
              (throw (IOException. "not a valid DICOM object")))
            obj)
          (catch IOException e (throw e))
          (catch Throwable e
            (throw (make-caused-IOException
                    e "Not a DICOM file, or an error occurred")))))))
  ([in-s] (read-stream in-s nil)))

(defn read-file
  "Reads a DICOM object from a file; f may be a File or String
filename. If the filename ends in .gz, assumes it is gzip compressed
and uncompresses the contents inline."
  ([f max-tag]
     (io!
      (with-open [in-s (FileInputStream. f)]
        (let [name (if (instance? File f) (.getName f) f)]
          (if (.endsWith name gzip-suffix)
            (with-open [in-gzs (GZIPInputStream. in-s)]
              (read-stream in-gzs max-tag))
            (read-stream in-s max-tag))))))
  ([f] (read-file f nil)))

(defn obj-seq
  "Generates a lazy sequence of [File, DICOM object] vectors
from a sequence of files."
  ([fs max-tag]
     (if (seq fs)
       (lazy-seq
	(let [f (first fs)
              file (if (instance? File f) f (File. f))]
	  (try (cons [file (read-file file max-tag)]
		     (obj-seq (rest fs) max-tag))
	       (catch Throwable t
		 (obj-seq (rest fs) max-tag)))))
       '()))
  ([fs] (obj-seq fs nil)))

(declare to-map)

(defn to-seq [dicom-element specific-charset cache?]
  "Extract the value of the provided DicomElement into a sequence
of suitable JVM representations."
  (let [vr (.vr dicom-element)]
    (seq (condp contains? vr
           #{VR/AE VR/AS VR/CS VR/DS VR/IS VR/LO VR/LT VR/PN VR/SH
             VR/ST VR/UI VR/UT}
           (.getStrings dicom-element specific-charset cache?)
             
           #{VR/AT VR/SL VR/SS VR/UL VR/US}
           (.getInts dicom-element cache?)
               
           #{VR/DA VR/DT VR/TM}
           (.getDates dicom-element cache?)

           #{VR/FL VR/OF}
           (.getFloats dicom-element cache?)

           #{VR/FD}
           (.getDoubles dicom-element cache?)
           
           #{VR/OB VR/OW VR/UN}
           (.getBytes dicom-element)
           
           #{VR/SQ} (map to-map
                      (for [i (range (.countItems dicom-element))]
                        (.getDicomObject dicom-element i)))))))

                                     
(defn to-map [dicom-object & {:keys [cache-elems]
                              :or {cache-elems false}}]
  "Turn the provided DicomObject into a map. Any contained DICOM
sequences will be converted to sequences, possibly containing nested
maps."
  (let [specific-charset (.getSpecificCharacterSet dicom-object)]
    (into {} (map #(vector (.tag %)
                           (to-seq % specific-charset cache-elems))
                  (iterator-seq (.iterator dicom-object))))))

(defn to-hounsfield
  "HU = m * P + b
      where:
          m = dicom attribute (0028,1053) Rescale slope
          b = dicom attribute (0028,1052) Rescale intercept
          P = value of pixel/voxel"
  [m P b]
  (+ (* m P) b))

(defn to-short
  [[low high]]
  (bit-or (bit-shift-left high 8) low))

(defn get-metadata
  [path]
  (-> path
      read-file
      to-map))

(def tags {:pixel-data   0x7FE00010
           :image-number 0x00200013})

(defn get-pixels
  [metadata]
  (metadata (:pixel-data tags)))

(defn get-image-number
  [metadata]
  (metadata (:image-number tags)))

(defn extract-pixel-data
  [path]
  (-> path
      get-metadata
      get-pixels))

(defn load-image-data
  [path]
  (->> path
       extract-pixel-data
       (partition 2)
       (map to-short)
       (map #(to-hounsfield 1 % -1024))))

(defn- pixel-entry
  [z y pixels]
  (map-indexed (fn [x value] (vector [x y z] {:v value})) pixels))

(defn build-pixels
  [path slice-index]
  (let [pixels (load-image-data path)
        z slice-index
        rows (partition 512 pixels)]
    (map-indexed (fn [y row] (pixel-entry z y row)) rows)))

(defn slice-order
  [file]
  (vector (java.lang.Integer/parseInt (first (get-image-number (get-metadata file)))) (.getPath file)))

(defn order-files-by-slice
  [path]
  (let [files (rest (file-seq (io/file path)))]
    (sort-by first (pmap #(slice-order %) files))))

(defn get-slices
  [path start count]
  (let [files (order-files-by-slice path)]
    (take count (drop start files))))
