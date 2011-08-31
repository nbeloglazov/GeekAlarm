(ns com.android.server.generators.regex
  (:require [clojure.contrib
             [seq-utils :as seq-utils]]
            [com.geekalarm.server
             [utils :as utils]]))

(defn phrase []
  (rand-nth ["Hello, world",
             "Simple regex",
             "I love regexps",
             "I hate regexps",
             "Regexps are awesome",
             "Regexps are terrible"]))

(defn correct-task? [[source goal [correct choices]]]
  (let [cor (nth choices (dec correct))
        incor (remove #(= cor %) choices)
        res-cor (re-seq (re-pattern cor) source)
        res-incor (map #(re-seq (re-pattern %) source) incor)]
    (and (= res-cor [goal])
         (every? empty? res-incor))))

(defn random-substring [word]
  (let [len (count word)
        start (rand-int (- len 4))
        sub-len (+ 2 (rand-int (- len start 2)))]
    (subs word start (+ start sub-len))))

(defn random-char []
  (->> (range (int \a) (inc (int \z)))
       (concat (range (int \A) (inc (int \Z))))
       (rand-nth)
       (char)))

(defn similar-string [string]
  (let [pos (rand-int (count string))]
    (apply str (assoc (vec string)
                 pos
                 (random-char)))))

(defn simplest-task []
  (let [generate (fn []
                   (let [source (phrase)
                         goal (random-substring source)
                         choices (utils/get-similar-by-fn goal similar-string)]
                     [source goal choices]))]
    (seq-utils/find-first correct-task? (repeatedly generate))))

