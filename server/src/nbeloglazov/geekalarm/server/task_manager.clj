(ns nbeloglazov.geekalarm.server.task-manager
  (:require [nbeloglazov.geekalarm.server.render-utils :as render]
	    [nbeloglazov.geekalarm.server.generators
	     [determinant :as determinant]
             [definite-polynomial-integral :as definite-polynomial-integral]
             [inverse-matrix :as inverse-matrix]
             [base-conversion :as base-conversion]
             [derivative :as derivative]
             [matrix-multiplication :as matrix-multiplication]]))

(def categories [:linear-algebra :math-analysis :computer-science])

(def generators
     {:linear-algebra [determinant/generate
                       inverse-matrix/generate
                       matrix-multiplication/generate]
      :math-analysis  [definite-polynomial-integral/generate
                       derivative/generate]
      :computer-science [base-conversion/generate]})

(def description
     {:linear-algebra {:name "Linear algebra"}
      :math-analysis {:name "Mathematical analysis"}
      :computer-science {:name "Computer science"}})

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

