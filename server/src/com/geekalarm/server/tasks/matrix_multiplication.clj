(ns com.geekalarm.server.tasks.matrix-multiplication
  (:require [incanter.core :as incanter]
            [com.geekalarm.server
             [utils :refer (random-matrix get-similar-matrices)]
             [latex-utils :refer (matrix)]]))

(def maxs [9 19 9])

(def sizes [2 2 3])

(defn generate [level]
  (let [[a b] (->> [(sizes level) (maxs level)]
                   (apply partial random-matrix)
                   (repeatedly 2))
        c (incanter/mmult a b)
        [correct choices] (get-similar-matrices c)]
    {:question (str (matrix a "()")
                    "\\times"
                    (matrix b "()")
                    "=?")
     :choices (map #(matrix % "()") choices)
     :correct correct}))

(def info {:type :matrix-multiplication
           :name "Matrix multiplication"
           :description (str "Find product of the matrices.\n"
                             "http://en.wikipedia.org/wiki/Matrix_multiplication")
           :generator #'generate})
