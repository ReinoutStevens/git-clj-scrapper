(ns git-scrapper.localrepository
  (:require [clj-jgit 
             [porcelain :as porcelain]
             [querying :as querying]
             [internal :as internal]])
  (:require [clojure.java.io :as io])
  (:require [git-scrapper.meta :as meta]))
            


(defn convert-repository [repo walker]
  "loops over al the commits in the repository and returns
a hash where the keys are the sha of each version, and the value is the corresponding
metaversion instance."
  (let [revcommits (querying/rev-list repo)]
    (defn loop-commits []
      (reduce
       (fn [hash commit]
         (let [info (querying/commit-info-without-branches repo walker commit)
               id (:id info)
               predecessors (map #(:id (querying/commit-info-without-branches repo walker %))
                                 (seq (.getParents commit)))
               author (:author info)
               message (:message info)
               changed_files  (map (fn [[name type]]
                                        (meta/->ChangedFile name type))
                                   (:changed_files info))
               time (:time info)]
           (assoc hash
             id (meta/->MetaVersion id predecessors '() time changed_files author message))))
       {}
       revcommits))
    (defn finalize-versions [hash]
      (let [versions (vals hash)]
        (reduce
         (fn [h version] ;;metaversion
           (reduce
            (fn [nested-hash pred] ;;predecessorstring
              (let [predversion (get nested-hash pred)] ;;metaversion
                (assoc nested-hash
                  pred
                  (meta/version-add-successor predversion (:sha version)))))
            h
            (:predecessors version)))
         hash
         versions)))
    (finalize-versions (loop-commits))))


(defn repository-name [location]
  (.getName (.getParentFile location)))

(defn create-project-from-local-repository [repo-location startstr]
  (let [repository (clj-jgit.porcelain/load-repo repo-location)
        walker  (clj-jgit.internal/new-rev-walk repository)
        versionhash (convert-repository repository walker)
        repo-file (io/file repo-location)
        metarepo (meta/->MetaRepository repo-file)
        name (repository-name repo-file)]
    (meta/->MetaProject name versionhash metarepo)))
