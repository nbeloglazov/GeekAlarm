(ns nbeloglazov.geekalarm.server.generators.derivative
  (:require [nbeloglazov.geekalarm.server
             [derivative-utils :as der-ut]
             [utils :as utils]]
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

(defn get-similar-expr [expr]
     (let [[is-fn rep-fn] (-> [[#(= % :x)
                                (fn [_] [:pow :x (+ 2 (rand-int 4))])]
                               [number?
                                #(+ % (dec (* 2 (rand-int 2))))]
                               [#(and (coll? %)
                                      (contains? der-ut/fns (first %)))
                                (fn [[name & args]] (cons (rand-fn) args))]]
                              (rand-nth))
           n (->> (flatten expr)
                  (filter is-fn)
                  (count)
                  (rand-int))
           cnt (atom (inc n))]
       (walk/postwalk #(if (and (is-fn %)
                                (zero? (swap! cnt dec)))
                         (rep-fn %)
                         %)
                      expr)))

(defn generate [level]
  (let [expr (->> ([easy medium hard] level)
                  (rand-nth)
                  (#(%))
                  (der-ut/normalize))
        derivative (->> expr
                        (der-ut/derivative)
                        (der-ut/normalize))
        [correct choices] (utils/get-similar-by-fn derivative
                                                   get-similar-expr)]

    {:question (der-ut/to-cljml expr)
     :choices (map der-ut/to-cljml (map der-ut/normalize choices))
     :correct correct}))
        
          
           
           