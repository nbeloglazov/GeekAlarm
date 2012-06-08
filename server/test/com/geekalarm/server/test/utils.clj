(ns com.geekalarm.server.test.utils
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

(deftest test-get-similar-by-one
  (dotimes [_ 10]
    (let [x (rand-int 100)
          [first-pos shuffled] (get-similar-by-one x)]
      (is (= (nth shuffled first-pos)
             x))
      (is (every? #(= -1 %)
                  (->> (sort shuffled)
                       (partition 2)
                       (map #(apply - %))))))))

(deftest test-rand-range
  (dotimes [_ 10]
    (let [a (rand-int 100)
          b (+ a (rand-int 100))]
      (dotimes [_ 10]
        (is (<= a (rand-range a b) b))))))



