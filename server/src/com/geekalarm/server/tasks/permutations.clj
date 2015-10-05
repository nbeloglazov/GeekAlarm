(ns com.geekalarm.server.tasks.permutations
  (:require [com.geekalarm.server
             [utils :as u]
             [latex-utils :as lu]]
            [clojure.string :as cstr]
            [clojure.math.combinatorics :as combo]))

(def sets ["▤▣▢▩▥▲△▶▷▼▽◀◁○◉◍◎◐◌"
           "☀☁☂☃★☆☎☏☘☢☮☭☯☾"
           "♈♉♊♋♌♍♎♏♐♑♒♓"
           "♔♕♖♗♘♙♚♛♜♝♞♟"
           "⚀⚁⚂⚃⚄"
           "12345"])

(def ranges [[3 5] [3 4] [2 5]])

(defn generate-items [level]
  (let [set (-> sets rand-nth seq shuffle)
        un-items (->> (nth ranges level)
                      (apply u/rand-range)
                      (#(take % set)))
        repeated-n (nth [0 (u/rand-range 1 2) 3] level)
        repeated (repeatedly repeated-n #(rand-nth un-items))]
    (shuffle (concat un-items repeated))))

(defn similar-answer [answer]
  (let [modifiers (for [num [2 3 5 7 10]
                        op [* /]
                        :let [f #(op % num)]
                        :when (not (ratio? (f answer)))]
                    f)]
    ((rand-nth modifiers) answer)))

(defn generate [level]
  (let [items (generate-items level)
        answer (combo/count-permutations items)
        [correct choices] (u/get-similar-by-fn answer similar-answer)]
    {:question (lu/lines (map lu/text
                              [(str "Number of permutations?")
                               (cstr/join ", " items)])
                         "c")
     :correct correct
     :choices (map lu/to-latex choices)}))

(def info
  {:type :permutations
   :name "Permutations"
   :generator #'generate
   :description (str "Find number of permutations of given objects.\n"
                     "https://en.wikipedia.org/wiki/Permutation")})
