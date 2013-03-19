(ns git-scrapper.gitub
  (:require [tentacles
             [repos :as repos])))




(defn process-github-repo [user reponame]
  (let [repo (repos/specific-repo user reponame)
        clone-url (:clone_url repo)]
    
