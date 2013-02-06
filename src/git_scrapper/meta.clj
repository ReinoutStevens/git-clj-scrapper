(ns git-scrapper.meta
  (:use [clj-jgit porcelain querying internal])
  (:use [clojure.java.io]))


(defn meta-version [repo commit predecessors successors]
  {:commit commit
   :predecessors predecessors
   :successors successors
   :removed-predecessors '()
   :removed-successors '()
   :repo repo
   :info (commit-info repo commit)})


(defn add-predecessor [version pred]
  (assoc version
    :predecessors
    (cons pred (:predecessors version))))

(defn add-successor [version succ]
  (assoc version
    :successors
    (cons succ (:successors version))))

(defn version-get-info [version keysymbol]
  (keysymbol
   (:info version)))

(defn version-id [version]
  (version-get-info version :id))

(defn version-branches [version]
  (version-get-info version :branches))

(defn version-author [version]
  (version-get-info version :author))

(defn version-time [version]
  (version-get-info version :time))

(defn version-message [version]
  (version-get-info version :message))

(defn version-changed [version]
  (version-get-info version :changed_files))

(defn walk-the-walk [repo start]
  (let [walker (clj-jgit.internal/new-rev-walk repo)]
    (.markStart walker (.parseCommit walker start))
    (seq walker)))


(defn talk-the-talk [repo start]
  (let [revcommits (walk-the-walk repo start)]
    (defn loop-commits []
      (reduce
       (fn [hash commit]
         (let [info (commit-info repo commit)
               predecessors (map (fn [commit]
                                   (:id (commit-info repo commit)))
                                 (seq (.getParents commit)))
               id (:id info)]
           (assoc hash
             id (meta-version repo commit predecessors '()))))
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
                  (add-successor predversion (version-id version)))))
            h
            (:predecessors version)))
         hash
         versions)))
    (finalize-versions (loop-commits))))


(defn meta-project [repo start]
  (let [versions (talk-the-talk repo start)]
    {:versions versions
     :repo repo}))

(defn get-version [project id]
  (get (:versions project) id))





(defn repo-name [repo]
  (.getName (.getParentFile  (.getDirectory (.getRepository repo))))) ;;improve?

(defn repo-location [repo]
  (.getDirectory (.getRepository repo)))

(defn version-location [version]
  (let [repo (:repo version)
        id (version-id version)]
    (str (repo-name repo) "-" id)))

(defn prepare-new-repo [version destination]
  (let [repo (:repo version)
        location (.getAbsolutePath (repo-location repo)) ;;ugly :(
        new-file-location (file
                           destination
                           (str (repo-name repo)
                                "-"
                                (version-id version)))
        new-location  (.getAbsolutePath new-file-location )]
    (git-clone location new-location)))

(defn checkout-version [version destination]
  (let [branch (first (version-branches version))
        repo (prepare-new-repo version destination)]
    (git-checkout repo branch true false (:commit version))))


(defn checkout-project [project destination]
  (map (fn [v]
         (checkout-version v destination))
       (vals (:versions project))))
