(ns com.geekalarm.server.tasks.bitwise-operations
  (:require [com.geekalarm.server.utils :as utils]))

(def levels [[1 7]
             [1 31]
             [1 127]])

(def operations [["xor" bit-xor]
                 ["and" bit-and]
                 ["or" bit-or]])

(defn add-leading-zeroes [v]
  (let [need (count v)
        need (if (= 1 need) (inc need) need)
        need (if (even? need) (inc need) need)]
    (concat (repeat (- need (count v)) \0)
            v)))


(defn change-random-bit [n]
  (let [binary (-> (Integer/toBinaryString n) add-leading-zeroes vec)
        bit (rand-int (count binary))]
    (-> (update-in binary [bit] {\0 \1 \1 \0})
        (#(apply str %))
        (Integer/parseInt 2))))

(defn generate [level]
  (let [range (levels level)
        a (apply utils/rand-range range)
        b (apply utils/rand-range range)
        [op-name op-fn] (rand-nth operations)
        res (op-fn a b)
        [correct choices] (utils/get-similar-by-fn res
                                                   change-random-bit)]
    {:question (format "%s\\; \\text{%s} \\; %s = ?" a op-name b)
     :choices (map str choices)
     :correct correct}))

(def info {:name "Bitwise operations"
           :type :bitwise-operations
           :description (str "Calculate bitwise operator for 2 arguments.\n"
                             "http://en.wikipedia.org/wiki/Bitwise_operation")
           :generator #'generate})


