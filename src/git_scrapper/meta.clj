(ns git-scrapper.meta
   (:use [clojure.data.xml]))

;;Proper metamodel


(defrecord MetaRepository [url] ) ;;should probably get some more information...
(defrecord ChangedFile [filename change-type] )  
(defrecord MetaVersion [sha predecessors successors date changed-files author message] )

(defn version-add-predecessor [version pred]
  (update-in version [:predecessors] #(cons pred %)))

(defn version-add-successor [version succ]
  (update-in version [:successors] #(cons succ %)))

(defrecord MetaProject [name versions repository] )
(defrecord MetaProduct [projects] )


(defn project-find-version [project version-sha]
  (get  (:versions project) version-sha))


(defn product-find-version [product version-sha]
  (let [projects (:projects product)]
    (some #(project-find-version % version-sha) projects)))



;;XML Generation

(defn format-date [date]
  (let [formatter (new java.text.SimpleDateFormat "yyyy-MM-dd HH:mm:ss")]
    (.format formatter date)))


(defn changed-file-generate-xml [changed-file]
  (element :changed
           {:file (:filename changed-file)
            :type (name (:change-type changed-file))}))


(defn version-generate-xml [version]
  (let [revision (:sha version)
        author (:author version)
        message (:message version)
        time (:date version)
        changed (:changed-files version)]
  (element :version
   {:author author
    :revision revision
    :message (clojure.string/replace (clojure.string/replace  message (re-pattern "\\P{ASCII}") "") (re-pattern "\u0001") "")
    :time (format-date time) }
   (map
    (fn [pred]
      (element :predecessor
               {:revision pred}))
    (:predecessors version))
   (map
    (fn [succ]
      (element :successor
                {:revision succ}))
    (:successors version))
    (map
     changed-file-generate-xml
     changed))))



(defn repository-generate-xml [repository]
  (element
   :repository
   {:url (.getAbsolutePath (:url repository))}))


(defn project-generate-xml [project]
  (element :project
           {:name (:name project)}
           (repository-generate-xml (:repository project))
           (map version-generate-xml
                (vals (:versions project)))))



(defn product-generate-xml [product]
  (element :product {}
           (map
            project-generate-xml
            (:projects product))))
