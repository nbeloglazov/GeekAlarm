(ns com.geekalarm.server.tasks.base-arithmetic
  (:require [com.geekalarm.server.utils :as u]))

(defn get-values [level]
  (let [max (if (= level 0) 20 50)]
    [(inc (rand-int max))
     (inc (rand-int max))]))

(defn rand-base []
  (->> (range 2 17)
       (remove #(= 10 %))
       (rand-nth)))

(defn get-bases [level]
  (let [base (rand-base)
        second-base (u/find-matching-value rand-base
                                           #(not= base %))]
    [base
     (if (= level 2) second-base base)]))

(defn change-random-digit [value base]
  (let [d (rand-nth [1 -1])
        pow (rand-int 2)]
    (int (+ value (* d (Math/pow base pow))))))

(defn generate [level]
  (let [[v1 v2] (get-values level)
        [b1 b2] (get-bases level)
        sign (rand-nth [:+ :-])
        result (case sign
                 :+ (+ v1 v2)
                 :- (- v1 v2))
        [correct choices] (u/get-similar-by-fn result #(change-random-digit % b2))]
    {:question (format "%s %s %s= ?"
                       (u/to-base v1 b1)
                       (name sign)
                       (u/to-base v2 b1))
     :choices (map #(u/to-base % b2) choices)
     :correct correct}))

(def info {:name "Base arithmetic"
           :type :base-arithmetic
           :description (str "Calculate sum or difference of numbers in random bases\n"
                             "http://en.wikipedia.org/wiki/Positional_notation")
           :generator #'generate})

