(ns com.geekalarm.server.tasks.statistics
  (:require [com.geekalarm.server
             [utils :as u]
             [latex-utils :as lu]]
            [clojure.string :as cstr]
            [incanter.stats :as stats]))

(defn generate-set [n]
  (repeatedly n #(rand-int 11)))

(defn median [set]
  (let [n (count set)
        set (sort set)
        l (nth set (dec (quot n 2)))
        r (nth set (quot n 2))]
    (if (odd? n)
      r
      (/ (+ l r) 2))))

(defn generate-median [level]
  (let [n (->> level
               (nth [[4 5] [6 9] [10 15]])
               (apply u/rand-range))
        set (generate-set n)
        ans (median set)]
    {:name "median"
     :set set
     :choices (u/get-similar-by-one ans)}))

(defn generate-mean [level]
  (let [n (->> level
               (nth [[4 5] [9 10] [13 16]])
               (apply u/rand-range))
        set (generate-set n)
        sum (apply + set)
        [correct choices] (u/get-similar-by-one sum)]
    {:name "mean"
     :set set
     :choices [correct (map #(/ % n) choices)]}))

(defn generate [level]
  (let [gen-fn (rand-nth [generate-median generate-mean])
        {:keys [name set choices]} (gen-fn level)
        question [(str "Find " name " of the dataset:")
                  (cstr/join ", " set)]]
    {:question (lu/lines (map lu/text question) "c")
     :choices (map #(lu/to-latex % true) (second choices))
     :correct (first choices)}))

(def info {:name "Statistics"
           :type :statistics
           :description (str "Mean: https://en.wikipedia.org/wiki/Arithmetic_mean\n"
                             "Median: https://en.wikipedia.org/wiki/Median")
           :generator #'generate})

