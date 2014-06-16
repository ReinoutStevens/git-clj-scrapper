(ns git-scrapper.main
  (:gen-class)
  (:require [git-scrapper.core :as core]))


(defn -main [& args]
  (let [location (first args)
        target (second args)]
    (core/process-repository location target)))
