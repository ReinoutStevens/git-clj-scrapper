(ns git-scrapper.localrepository
  (:use (clj-jgit porcelain querying internal))
  (:use [clojure.java.io])
  (:require [git-scrapper.meta :as meta]))
            


(defn get-revcommits [repo start]
  (let [walker (clj-jgit.internal/new-rev-walk repo)]
    (.markStart walker (.parseCommit walker start))
    (seq walker)))


(defn convert-repository [repo start]
  "loops over al the commits in the repository and returns
a hash where the keys are the sha of each version, and the value is the corresponding
metaversion instance."
  (let [revcommits (get-revcommits repo start)]
    (defn loop-commits []
      (reduce
       (fn [hash commit]
         (let [info (commit-info repo commit)
               id (:id info)
               predecessors (map #(:id (commit-info repo %))
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
  (.getName location))



(defn create-project-from-local-repository [repo-location startstr]
  (let [repository (clj-jgit.porcelain/load-repo repo-location)
        walker  (clj-jgit.internal/new-rev-walk repository)
        startobj (resolve-object repository startstr)
        start (.parseCommit walker startobj)
        versionhash (convert-repository repository start)
        repo-file (file repo-location)
        metarepo (meta/->MetaRepository repo-file)
        name (repository-name repo-file)]
    (meta/->MetaProject name versionhash metarepo)))
