(ns nbeloglazov.geekalarm.server.generators.determinant
  (:require [incanter.core :as incanter])
  (:use [nbeloglazov.geekalarm.server.mathml-utils :only (cljml)]))

(defn- random-matrix [size max]
  (let [rnd #(- (rand-int (* 2 max)) max)]
  (-> (repeatedly (* size size) rnd)
      (incanter/matrix size))))

(defn- get-similar [x]
  (let [r (rand-int 4)
	a (- x r)]
    [(inc r) (range a (+ a 4))]))

(defn- question-to-cljml [matrix]
  [:math [:mo "|"]
         [:mphantom [:mtext "-"]] ; hack to insert some space between by bracker and numbers
         (cljml matrix)
         [:mphantom [:mtext "-"]]
         [:mo "|"]
         [:mo "="]
         [:mtext "?"]])

(def sizes [2 2 3])

(def maxs  [10 20 10])

(defn generate [level]
  (let [size (sizes (dec level))
	max (maxs (dec level))
	matrix (random-matrix size max)
	det (incanter/det matrix)
	[cor answers] (get-similar (int det))]
    {:question (question-to-cljml matrix)
     :choices (map cljml answers)
     :correct cor}))