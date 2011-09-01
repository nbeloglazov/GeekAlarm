(ns com.geekalarm.server.generators.prime-numbers
  (:require [clojure.contrib
             [lazy-seqs :as lazy-seqs]
             [seq-utils :as seq-utils]]
            [com.geekalarm.server
             [utils :as utils]]))

(def levels [[0 100]
             [100 500]
             [500 1500]])

(defn subseq-within [left right seq]
  (->> seq
       (take-while #(< % right))
       (drop-while #(< % left))))

(def pseudoprimes (memoize (fn []
  (let [mx (second (last levels))]
    (loop [primes (subseq-within 3 mx lazy-seqs/primes)
           accum []]
      (if (empty? primes)
        (sort accum)
        (recur (rest primes)
               (->> (map #(* (first primes) %) primes)
                    (take-while #(< % mx))
                    (concat accum)))))))))

(defn rand-prime [left right]
  (rand-nth (subseq-within left right lazy-seqs/primes)))

(defn rand-pseudoprime [left right]
  (rand-nth (subseq-within left right (pseudoprimes))))

(defn generate [level]
  (let [[left right] (levels level)
        pseudo-fn (fn [_] (rand-pseudoprime left right))
        prime (rand-prime left right)
        [correct choices] (utils/get-similar-by-fn prime pseudo-fn)]
    {:question [:mtext "Prime number?"]
     :choices (map (fn [x] [:mn x]) choices)
     :correct correct
     :name "Prime numbers"}))