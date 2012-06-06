(ns com.geekalarm.server.utils-test
  (:use clojure.test
        com.geekalarm.server.utils))

(defn rand-seq [n]
  (->> (repeatedly n rand)
       (reductions +)))

(deftest test-shuffle-and-track-first
  (doseq [i (range 1 10)]
    (let [s (rand-seq i)
          expected-first (first s)
          [first-pos shuffled] (shuffle-and-track-first s)]
      (is (= (nth shuffled first-pos) expected-first))
      (is (= (set shuffled) (set s))))))

(deftest test-get-similar-by-fn
  (dotimes [_ 10]
    (let [orig (rand-int 100)
          f #(+ % (rand-int 100))
          [orig-pos shuffled] (get-similar-by-fn orig f)]
      (is (= (nth shuffled orig-pos) orig))
      (is (apply distinct? shuffled))
      (is (every? #(<= orig % (+ orig 100)) shuffled)))))

(deftest test-prime?
  (are [x] (prime? x)
       2 3 5 7 11 13 17 907 997))

(deftest test-primes
  (is (= (take 10 (primes))
         [2 3 5 7 11 13 17 19 23 29])))

