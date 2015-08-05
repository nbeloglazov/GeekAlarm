(ns com.geekalarm.server.tasks.inverse-matrix
   (:require [incanter.core :as ic]
             [clojure.math.numeric-tower :as nt]
             [com.geekalarm.server
              [latex-utils :as lu]
              [utils :as u]]))

(def maxs [4 9 4])

(def sizes [2 2 3])

(defn get-matrix [size max]
  (u/find-matching-value
   #(let [mat (u/random-matrix size max)]
      [(nt/round (ic/det mat)) mat])
   #(pos? (first %))))

(defn generate [level]
  (let [n (sizes level)
        [det mat] (get-matrix n (maxs level))
        answer (ic/solve mat (ic/mult det (ic/identity-matrix n)))
        [correct choices] (u/get-similar-matrices answer)]
    {:question (str (lu/matrix mat "()") "^{-1} = ?")
     :choices (map #(lu/matrix % "()")
                   (for [mat choices]
                     (for [row mat]
                       (for [val row]
                         (/ (nt/round val) det)))))
     :correct correct}))

(def info {:type :inverse-matrix
           :name "Inverse matrix"
           :description (str "Find inverse of the matrix.\n"
                             "http://en.wikipedia.org/wiki/Invertible_matrix")
           :generator #'generate})


