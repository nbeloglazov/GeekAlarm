(ns com.geekalarm.server.tasks.inverse-matrix
   (:require [incanter.core :as incanter]
             [clojure.math.numeric-tower :refer (round)]
             [com.geekalarm.server
              [latex-utils :refer (matrix to-latex)]
              [utils :refer (get-similar-matrices
                             random-matrix)]]))

(def maxs [4 9 4])

(def sizes [2 2 3])

(defn get-matrix [size max]
  (->> (repeatedly #(random-matrix size max))
       (map (fn [mat]
              [(round (incanter/det mat)) mat]))
       (filter #(> (first %) 0))
       (first)))

(defn generate [level]
  (let [n (sizes level)
        [det mat] (get-matrix n (maxs level))
        answer (incanter/solve mat (incanter/mult det (incanter/identity-matrix n)))
        [correct choices] (get-similar-matrices answer)]
    {:question (str (matrix mat "()") "^{-1} = ?")
     :choices (map #(matrix % "()")
                   (for [mat choices]
                     (for [row mat]
                       (for [val row]
                         (/ (round val) det)))))
     :correct correct}))

(def info {:type :inverse-matrix
           :name "Inverse matrix"
           :description (str "Find inverse of the matrix.\n"
                             "http://en.wikipedia.org/wiki/Invertible_matrix")
           :generator #'generate})


