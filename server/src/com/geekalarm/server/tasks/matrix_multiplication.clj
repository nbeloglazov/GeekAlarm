(ns com.geekalarm.server.tasks.matrix-multiplication
  (:use [com.geekalarm.server
         [utils :only (random-matrix get-similar-matrices)]
         [mathml-utils :only (cljml)]])
  (:require [incanter.core :as incanter]))

(def maxs [9 19 9])

(def sizes [2 2 3])

(defn generate [level]
  (let [[a b] (->> [(sizes level) (maxs level)]
                   (apply partial random-matrix)
                   (repeatedly 2))
        c (incanter/mmult a b)
        [correct choices] (get-similar-matrices c)]
    {:question [:mrow
                [:mo "("]
                (cljml a)
                [:mo ")"]
                [:mo "&#215;"]
                [:mo "("]
                (cljml b)
                [:mo ")"]
                [:mo "="]
                [:mtext "?"]]
     :choices (map (fn [mat]
                     [:mrow
                      [:mo "("]
                      (cljml mat)
                      [:mo ")"]])
                   choices)
     :correct correct}))

(def info {:id :matrix-multiplication
           :name "Matrix multiplication"
           :description (str "Find product of the matrices.\n"
                             "http://en.wikipedia.org/wiki/Matrix_multiplication")
           :generator generate})
