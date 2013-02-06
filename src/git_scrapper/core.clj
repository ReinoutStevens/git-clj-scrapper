(ns git-scrapper.core
  (:use [git-scrapper.meta])
  (:use [git-scrapper.xmlgeneration])
  (:use [clj-jgit porcelain querying internal])
  (:use [clojure.java.io])
  (:use [clojure.tools.cli :only [cli]])
  (:use [clojure.data.xml]))




(defn create-project
  ([repo-location]
   (create-project repo-location "head"))
  ([repo-location startstr]
   (let [repository (clj-jgit.porcelain/load-repo repo-location)
         walker  (clj-jgit.internal/new-rev-walk repository)
         startobj (resolve-object repository startstr)
         start (.parseCommit walker startobj)
         project (meta-project repository start)]
     project)))



(defn process-repo [repo-location target-location]
  (let [project (create-project repo-location)
        name (repo-name (:repo project))
        f (file target-location)]
    ;;generate xml
    (when (.exists f)
      (throw (Exception. (str "output file already exists" (.getAbsolutePath f)))))
    (.mkdirs (file (.getParent f)))
    (.createNewFile f)
    (with-open [out-file (java.io.OutputStreamWriter.
                          (java.io.FileOutputStream. f) "UTF8")]
      (emit (project-generate-xml project) out-file))))

    
                                              
