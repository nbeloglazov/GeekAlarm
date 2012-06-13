(ns com.geekalarm.server.tasks.definite-polynomial-integral
  (:use [com.geekalarm.server
         [mathml-utils :only (cljml)]
         [utils :only (get-similar-by-one)]]
        [clojure.math.numeric-tower :only (expt)]))

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

(defn part-to-cljml [part]
  (let [{pow :power mult :multiplier} part
        m (when (or (not= mult 1)
                  (zero? pow))
            [:mn mult])
        b (when (not= pow 0)
            (if (not= pow 1)
              [:msup [:mi "x"]
                     [:mn pow]]
              [:mi "x"]))]
    (remove nil? [m b])))

(defn integral-to-cljml [integral]
  (let [parts (map part-to-cljml (:parts integral))
        one? (= (count parts) 1)
        parts-pluses (->> (interpose [[:mo "+"]] parts)
                          (apply concat))
        limit (:limit integral)
        expr [[:munderover
               [:mo "&#8747;"]
               [:mn (- limit)]
               [:mn limit]]
               (cljml :row (concat [(if one? [] [:mo "("])]
                                   parts-pluses
                                   [[:mo (if one? " " ")")]]))
               [:mi "d"]
               [:mi "x"]
               [:mo "="]
               [:mtext "?"]]]
    (cljml :math expr)))

(defn generate [level]
  (let [integral (generate-integral (sizes level))
        result (calculate-integral integral)
        [correct choices] (get-similar-by-one result)]
    {:question (integral-to-cljml integral)
     :choices (map cljml choices)
     :correct correct}))

(def info {:type :definite-integral
           :name "Definite integral"
           :description (str "Calculate given integral.\n"
                             "http://en.wikipedia.org/wiki/Symbolic_integration#Example")
           :generator generate})
