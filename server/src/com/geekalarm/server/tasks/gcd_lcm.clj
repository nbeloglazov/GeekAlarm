(ns com.geekalarm.server.tasks.gcd-lcm
  (:require [com.geekalarm.server
             [utils :refer (rand-range primes get-similar-by-fn)]
             [latex-utils :refer (to-latex text)]]))

(def difficulty [[2 100]
                 [100 1000]
                 [1000 10000]])

(defn gcd [a b]
  (if (zero? b)
    a
    (recur b (mod a b))))

(defn co-primes? [a b]
  (= 1 (gcd a b)))

(defn good-pair? [[a b]]
  (let [gcd (gcd a b)]
    (and (> gcd 1)
         (some #(> gcd (/ % 10)) [a b]))))

(defn generate-pair [[lower upper]]
  (->> #(vector (rand-range lower upper)
                (rand-range lower upper))
       repeatedly
       (filter good-pair?)
       first))

(defn divisors [num]
  (loop [divs #{}
         num num
         primes (primes)]
    (let [prime (first primes)]
      (if (>= num prime)
        (if (zero? (mod num prime))
          (recur (conj divs prime)
                 (quot num prime)
                 primes)
          (recur divs num (rest primes)))
        (if (empty? divs) #{1} divs)))))

(defn similar [num]
  (let [n (inc (rand-int 2))
        change-divisor (fn [num]
                         (let [divs (divisors num)]
                           ((rand-nth [+ -])
                            num (rand-nth (vec divs)))))]
    (nth (iterate change-divisor num) n)))

(defn generate [level]
  (let [[a b] (-> level difficulty generate-pair)
        type (rand-nth [:gcd :lcm])
        value (if (= type :gcd)
                (gcd a b)
                (/ (* a b) (gcd a b)))
        [correct choices] (get-similar-by-fn value similar)]
    {:question (str (text (if (= type :gcd) "GCD" "LCM"))
                     "(" a ", " b ") = ?")
     :choices (map to-latex choices)
     :correct correct}))

(def info {:type :gcd-lcm
           :name "GCD/LCM"
           :description (str "Find greatest common divisor of the given numbers:\n"
                             "http://en.wikipedia.org/wiki/Greatest_common_divisor\n"
                             "or least common multiple:\n"
                             "http://en.wikipedia.org/wiki/Least_common_multiple")
           :generator #'generate})


