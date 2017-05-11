(defproject git-scrapper "1.0.1-SNAPSHOT"
  :description "Converts a git repository to an xml file that can be imported by qwalkeko"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-jgit "0.8.9"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/tools.cli "0.3.1"]
                 [lacij "0.8.0"]]
  :main git-scrapper.main
  :aot [git-scrapper.main])
