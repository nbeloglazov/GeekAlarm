(ns com.geekalarm.server.tasks.determinant
  (:require [incanter.core :as incanter]
            [com.geekalarm.server
             [latex-utils :refer (to-latex matrix)]
             [utils :refer (get-similar-by-one
                            random-matrix)]]))

(def sizes [2 2 3])

(def maxs  [9 19 9])

(defn generate [level]
  (let [size (sizes level)
	max (maxs level)
	mat (random-matrix size max)
	det (incanter/det mat)
	[correct answers] (get-similar-by-one (Math/round det))]
    {:question (str (matrix mat "||") " = ?")
     :choices (map to-latex answers)
     :correct correct}))

(def info {:type :determinant
           :name "Determinant"
           :description (str "Find determinant of the matrix.\n"
                             "http://en.wikipedia.org/wiki/Determinant")
           :generator #'generate})