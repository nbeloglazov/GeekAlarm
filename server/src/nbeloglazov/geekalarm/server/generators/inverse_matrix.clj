(ns nbeloglazov.geekalarm.server.generators.inverse-matrix
  (:require [incanter.core :as incanter])
  (:use [clojure.contrib
         [seq-utils :only (find-first)]
         [math :only (round)]]
        [nbeloglazov.geekalarm.server
         [mathml-utils :only (cljml)]
         [utils :only (get-similar-matrices
                       random-matrix)]]))

(def maxs [4 9 4])

(def sizes [2 2 3])

(defn get-matrix [size max]
  (->> (repeatedly #(random-matrix size max))
       (map (fn [mat]
              [(round (incanter/det mat)) mat]))
       (find-first #(> (first %) 0))))

(defn generate [level]
  (let [n (sizes level)
        [det mat] (get-matrix n (maxs level))
        answer (incanter/solve mat (incanter/mult det (incanter/identity-matrix n)))
        [correct choices] (get-similar-matrices answer)]
    {:question [:math
                [:msup
                 [:mrow
                  [:mo "("]
                  (cljml mat)
                  [:mo ")"]]
                 [:mn -1]]
                [:mo "="]
                [:mtext "?"]]
     :choices (->> choices
                   (map (fn [mat]
                          (map (fn [row]
                                 (map #(/ (round %) det) row))
                               mat)))
                   (map (fn [mat]
                          [:math
                           [:mo "("]
                           (cljml mat)
                           [:mo ")"]])))
     :correct correct
     :name "Inverse matrix"}))
                  

