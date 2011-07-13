(ns nbeloglazov.geekalarm.server.generators.derivative
  (:require [nbeloglazov.geekalarm.server.derivative-utils :as der-ut]
            [clojure.walk :as walk])
  (:use [nbeloglazov.geekalarm.server.mathml-utils :only (cljml)]))

(defn rand-fn []
  (rand-nth (vec der-ut/fns)))

(defn rand-polynom []
  (->> (range 6)
       (shuffle)
       (take 3)
       (sort >)
       (map (fn [pow] [:pow :x pow]))
       (cons :plus)))

(def easy [#(vector (rand-fn) :x)
           rand-polynom])

(def medium
     [(fn []
        (let [[inner outer] (->> (map #(%) easy)
                                 (shuffle))]
          (walk/postwalk #(if (= % :x) inner %) outer)))])

(def hard
     [(fn []
        (let [a ((rand-nth easy))
              b ((rand-nth medium))
              op (rand-nth [:mult :div])]
          [op a b]))])

(defn generate [level]
  (let [expr (->> ([easy medium hard] level)
                  (rand-nth)
                  (#(%))
                  (der-ut/normalize))
        derivative (->> expr
                        (der-ut/derivative)
                        (der-ut/normalize))]
    {:question (der-ut/to-cljml expr)
     :choices (replicate 4 (der-ut/to-cljml derivative))
     :correct 1}))
        
          
           
           