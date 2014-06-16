(defproject git-scrapper "1.0.0-SNAPSHOT"
  :description "Converts a git repository to an xml file that can be imported by qwalkeko"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 ;;[dsabanin-clj-jgit "0.1.3"]
                 [clj-jgit "0.2.1"]
                 [org.clojure/data.xml "0.0.6"]
                 [org.clojure/tools.cli "0.2.2"]
                 [seesaw "1.4.2"]
                 [lacij "0.8.0"]
                 [tentacles "0.2.4"]]
  :main git-scrapper.main
  :aot [git-scrapper.main])
