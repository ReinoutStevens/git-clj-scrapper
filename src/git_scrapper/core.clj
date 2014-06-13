(ns git-scrapper.core
  (:require [git-scrapper.localrepository :as local] )
  (:require [git-scrapper.meta :as meta])
  (:use [clojure.java.io])
  (:use [clojure.tools.cli :only [cli]])
  (:use [clojure.data.xml]))


(defn create-project
  ([repo-location]
     (create-project repo-location "head"))
  ([repo-location startstr]
     (local/create-project-from-local-repository repo-location startstr)))


        


(defn create-product [& repository-locations]
  (let [projects (map create-project repository-locations)]
    ;;TODO link projects together
    (meta/->MetaProduct projects)))
      


(defn process-product [product target-location]
  (let [output (file target-location)]
    (when (.exists output)
      (throw (Exception. (str "output file already exists" (.getAbsolutePath output)))))
    (.mkdirs (file (.getParent output)))
    (.createNewFile output)
    (with-open [out-file (java.io.OutputStreamWriter.
                          (java.io.FileOutputStream. output) "UTF8")]
      (emit
       (meta/product-generate-xml product)
       out-file))))


(defn process-repository [location target]
  (let [product (create-product location)]
    (process-product product target)
    nil))
