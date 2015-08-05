(ns com.geekalarm.server.tasks.coins-combinations
  (:require [com.geekalarm.server
             [utils :as u]
             [latex-utils :as lu]]
            [clojure.string :as cstr]))

(defn calc-combinations
  "Calculates number of ways to get sum using given coins denominations"
  [amount coins]
  (letfn [(apply-coin [state coin]
            (reduce (fn [state val]
                      (if (<= (+ val coin) amount)
                        (update-in state [(+ val coin)] + (state val))
                        state))
                    state (range (inc amount))))]
    (last (reduce apply-coin
                  (-> (repeat (inc amount) 0) vec (assoc 0 1))
                  (sort coins)))))

(defn good-setup? [setup level]
  (let [answer (:answer setup)]
    (case level
      0 (<= 2 answer 3)
      1 (<= 4 answer 7)
      2 (<= 8 answer 10))))

(defn generate-setup [level]
  (let [sum (apply u/rand-int-within
                   (case level
                     0 [6 9]
                     1 [10 12]
                     2 [10 14]))
        n (apply u/rand-int-within
                 (case level
                   0 [1 2]
                   1 [2 3]
                   2 [4 5]))
        coins (->> (range 1 sum)
                   shuffle
                   (take n))]
    {:sum sum
     :coins (sort coins)
     :answer (calc-combinations sum coins)}))

(defn pos-choices? [[correct choices]]
  (every? pos? choices))

(defn generate [level]
  (let [{:keys [sum coins answer]} (u/find-matching-value
                                    #(generate-setup level)
                                    #(good-setup? % level))
        [correct choices] (u/find-matching-value
                           #(u/get-similar-by-one answer)
                           pos-choices?)
        question [(str "Make up " sum)
                  (str "using coins " (cstr/join ", " coins) ".")
                  "Number of combinations?"]]
    {:question (lu/lines (map lu/text question) "c")
     :choices (map str choices)
     :correct correct}))

(def info {:name "Coins combinations"
           :type :coins-combinations
           :description (str "Example: make up 4 using 1 and 2 coins.\n"
                             "There are 3 possible combinations:\n"
                             "1, 1, 1, 1\n"
                             "1, 1, 2\n"
                             "2, 2")
           :generator #'generate})

