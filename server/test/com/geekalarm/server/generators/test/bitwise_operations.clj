(ns com.geekalarm.server.generators.test.bitwise-operations
  (:use clojure.test
        com.geekalarm.server.generators.bitwise-operations))

(defn count-bits [n]
  (->> (Integer/toBinaryString n)
       (filter #(= \1 %))
       count))


(deftest test-change-random-bit
  (dotimes [_ 10]
    (let [n (rand-int 100)
          changed (change-random-bit n)]
      (is (= 1 (Math/abs (- (count-bits n)
                            (count-bits changed))))))))
