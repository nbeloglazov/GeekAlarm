(ns com.geekalarm.server.tasks.base-conversion
  (:require [com.geekalarm.server.utils :refer (get-similar-by-one)]
            [clojure.core.strint :refer (<<)]))

(defn generate [level]
  (let [a (case level
                0 10
                1 10
                2 (rand-nth (range 2 17)))
        b (case level
                0 2
                1 (rand-nth (concat (range 2 10)
                                    (range 11 17)))
                2 (->> (range 2 17)
                       (remove #(= a %))
                       (rand-nth)))
        val (+ (rand-int 50))
        [correct choices] (get-similar-by-one val)
        [from to] (shuffle [a b])]
    {:question (format "%s_{%s} = ?_{%s}"
                       (.toUpperCase (Integer/toString val from))
                       from
                       to)
     :choices (->> (map #(Integer/toString % to) choices)
                   (map #(.toUpperCase %))
                   (map (fn [val]
                          (format "%s_{%s}" val to))))
     :correct correct}))

(def info {:name "Base conversion"
           :type :base-conversion
           :description (str "Convert given number from one base to another.\n"
                             "http://en.wikipedia.org/wiki/Positional_notation")
           :generator #'generate})

