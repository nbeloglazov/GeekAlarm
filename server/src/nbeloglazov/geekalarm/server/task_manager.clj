(ns nbeloglazov.geekalarm.server.task-manager
  (:require [nbeloglazov.geekalarm.server.render-utils :as render]
	    [nbeloglazov.geekalarm.server.generators
	     [determinant :as determinant]]))

(def categories [:linear-algebra])

(def generators
  {:linear-algebra [determinant/generate]})

(def description
  {:linear-algebra {:name "Linear algebra"}})

(defn get-task [category level]
  (let [task ((rand-nth (generators category)) level)]
    (if (vector? (:question task))
      (render/render-cljml-task task)
      task)))

(defn get-categories []
  (map (fn [categ]
	 {:code (name categ)
	  :name (:name (description categ))})
       categories))

