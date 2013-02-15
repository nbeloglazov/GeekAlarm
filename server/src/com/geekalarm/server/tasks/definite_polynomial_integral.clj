(ns com.geekalarm.server.tasks.definite-polynomial-integral
  (:require [com.geekalarm.server
             [utils :refer (get-similar-by-one)]
             [latex-utils :refer (to-latex)]]
            [clojure.math.numeric-tower :refer (expt)]
            [clojure.string :refer (join)]))

(def max-limit 4)

(def max-power 3)

(def max-multiplier 5)

(def sizes [1 2 3])

(defn generate-integral [size]
  (let [parts (->> (range (inc max-power))
                   (shuffle)
                   (take size)
                   (sort >)
                   (map (fn [power] {:power power
                                     :multiplier (inc (rand-int max-multiplier))})))]
    {:parts parts
     :limit (inc (rand-int max-limit))}))

(defn calculate-integral [integral]
  (let [limit (:limit integral)
        calc-part (fn [{:keys [power multiplier]}]
                    (let [pow (inc power)
                          mult (/ multiplier pow)]
                      (* mult (- (expt limit pow)
                                 (expt (- limit) pow)))))]
    (->> (:parts integral)
         (map calc-part)
         (reduce +))))

(defn part-to-latex [{:keys [power multiplier]}]
  (let [m (if (or (not= multiplier 1)
                  (zero? power))
            multiplier
            "")
        b (if (not= power 0)
            (format "x^{%s}" (if (= 1 power) "" power))
            "")]
    (str m b)))

(defn integral-to-latex [{:keys [parts limit]}]
  (let [parts (map part-to-latex parts)
        one? (= (count parts) 1)
        parts-pluses (join "+" parts)]
    (format "\\int_{%s}^{%s} %s dx = ?"
            (- limit)
            limit
            (str (if one? "" "(")
                 parts-pluses
                 (if one? "" ")")))))

(defn generate [level]
  (let [integral (generate-integral (sizes level))
        result (calculate-integral integral)
        [correct choices] (get-similar-by-one result)]
    {:question (integral-to-latex integral)
     :choices (map to-latex choices)
     :correct correct}))

(def info {:type :definite-integral
           :name "Definite integral"
           :description (str "Calculate given integral.\n"
                             "http://en.wikipedia.org/wiki/Symbolic_integration#Example")
           :generator #'generate})
