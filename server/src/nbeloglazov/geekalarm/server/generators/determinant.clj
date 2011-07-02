(ns nbeloglazov.geekalarm.server.generators.determinant
  (:require [incanter.core :as incanter])
  (:use [nbeloglazov.geekalarm.server.mathml-utils :only (cljml)]))

(defn random-matrix [size]
  (-> (repeatedly (* size size) #(rand-int 10))
      (incanter/matrix size)))

(defn get-similar [x]
  (range x (+ x 4)))

(defn question-to-cljml [matrix]
  [:math [:mo "|"]
         [:mphantom [:mtext "-"]] ; hack to insert some space between by bracker and numbers
         (cljml matrix)
         [:mphantom [:mtext "-"]]
         [:mo "|"]
         [:mo "="]
         [:mtext "?"]])

(defn generate [level]
  (let [matrix (random-matrix 2)
	det (incanter/det matrix)
	answers (get-similar (int det))]
    {:question (question-to-cljml matrix)
     :choices (map cljml answers)
     :correct 1}))