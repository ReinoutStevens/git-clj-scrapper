(ns git-scrapper.xmlgeneration
  (:use [git-scrapper.meta])
  (:use [clojure.data.xml]))

(defn version-generate-xml [version]
  (let [revision (version-id version)
        author (version-author version)
        commit (version-message version)
        time (version-time version)
        location (version-location version)
        changed (version-changed version)]
  (element :version
   {:author author
    :revision revision
    :location location
    :commit commit }
   (element :time
            {:day (.getDate time)
             :year (+ 1900 (.getYear time))
             :month (inc (.getMonth time))
             :hour (.getHours time)
             :min (.getMinutes time)
             :sec (.getSeconds time)})
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
     (fn [[filename type]]
       (element :changed {:file filename :type (name type)}))
     changed))))

(defn project-generate-xml [project]
  (let [repo (:repo project)
        location (repo-location repo)
        url (.getAbsolutePath location)
        name (repo-name repo)]
    (element :project
             {:url url
              :name name}
             (map version-generate-xml
                  (vals (:versions project))))))
