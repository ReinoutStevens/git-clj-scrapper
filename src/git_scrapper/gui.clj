(comment
  https://gist.github.com/daveray/1441520
  https://github.com/pallix/lacij/blob/master/src/lacij/examples/swing.clj

(ns git-scrapper.gui
  (:require [seesaw.core :as seesaw])
  (:require [lacij.edit.graph :as graph]))
            


(seesaw/native!)


(defn product-to-graph [product]
  (let [g (reduce
           #(graph/add-node (keyword (.toLowerCase (:name %)))
                            (:name %) :x 20:y 20)
           (graph/graph)
           (:projects product))]
    g))



(defn ask-user-for-parents [product]
  (let [model (map keys (map :versions (product :projects)))
        leftpane (seesaw/scrollable (seesaw/listbox :model model))
        rightpane (seesaw/scrollable (seesaw/listbox :model model))
        split (seesaw/left-right-split leftpane rightpane :divider-location 1/2)]
    ))
    


(defn create-gui []
  (let [main-frame (seesaw/frame :title "Scrapper")
        split (left-right-split (scrollable lb) (scrollable area) :divider-location 1/3))

        button (seesaw/button :text "Add Link")]
    (seeaw/listen button :action (fn [e] (seesaw/alert e "Thanks!")))
    

    (-> main-frame seesaw/pack! seesaw/show!))
    
)
