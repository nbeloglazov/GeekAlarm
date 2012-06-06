(ns com.geekalarm.server.generators.determinant
  (:require [incanter.core :as incanter])
  (:use [com.geekalarm.server
         [mathml-utils :only (cljml)]
         [utils :only (get-similar-by-one
                       random-matrix)]]))


(defn- question-to-cljml [matrix]
  [:math [:mo "|"]
         [:mphantom [:mtext "-"]] ; hack to insert some space between by bracker and numbers
         (cljml matrix)
         [:mphantom [:mtext "-"]]
         [:mo "|"]
         [:mo "="]
         [:mtext "?"]])

(def sizes [2 2 3])

(def maxs  [9 19 9])

(defn generate [level]
  (let [size (sizes level)
	max (maxs level)
	matrix (random-matrix size max)
	det (incanter/det matrix)
	[cor answers] (get-similar-by-one (int det))]
    {:question (question-to-cljml matrix)
     :choices (map cljml answers)
     :correct (inc cor)
     :name "Determinant"
     :info (str "Find determinant of the matrix.\n"
                "http://en.wikipedia.org/wiki/Determinant")}))