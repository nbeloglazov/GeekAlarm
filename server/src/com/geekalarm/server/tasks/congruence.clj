(ns com.geekalarm.server.tasks.congruence
  (:require [clojure.math.combinatorics :as combinatorics]
            [com.geekalarm.server
             [utils :as utils]
             [mathml-utils :as mathml]]))

(def modules [2 3 5 7 11 13 17 19])
(def multiplier 4)

(defn solve [eqs]
  (let [satisfy? (fn [[b mod] x]
                   (zero? (rem (- x b) mod)))
        satisfy-all? (fn [x]
                       (->> (map #(satisfy? % x) eqs)
                            (every? true?)))]
    [(first (filter satisfy-all? (iterate inc 1)))
     (reduce * (map second eqs))]))

(defn generate-equations [n]
  (->> (if (= 1 n) (filter #(> % 3) modules)
                   modules)
       (shuffle)
       (take n)
       (map (fn [mod] [(inc (rand-int (dec mod)))
                       mod]))))

(defn get-combinations [eqs]
  (->> (combinatorics/subsets eqs)
       (remove empty?)
       (sort-by count)
       (reverse)
       (take 4)))

(defn rand-with-module [[base module] upper-bound]
  (-> (- upper-bound base)
      (quot module)
      (rand-int)
      (* module)
      (+ base)))

(defn answers-generic-to-concrete [[[base mod] & rst]]
  (let [upper-bound (* multiplier mod)
        real-answer (+ base (* (rand-int multiplier) mod))
        not-real-answer? (fn [x] (-> (- base x)
                                     (rem mod)
                                     (zero?)
                                     (not)))
        rand-answer (fn [ans]
                      (->> #(rand-with-module ans upper-bound)
                           (repeatedly)
                           (filter not-real-answer?)
                           (first)))]
    (println base mod rst)
    (with-meta (cons real-answer (map rand-answer rst))
      {:not-real-answer? not-real-answer?})))

(defn add-missing [answers]
  (let [missing (- 4 (count answers))
        upper-bound (reduce max 10 answers)
        not-real-answer? (:not-real-answer? (meta answers))
        additional (->> #(rand-int upper-bound)
                        repeatedly
                        distinct
                        (filter not-real-answer?)
                        (remove (set answers))
                        (take missing))]
    (concat answers additional)))

(defn get-choices [eqs]
  (->> (get-combinations eqs)
       (map solve)
       (answers-generic-to-concrete)
       (add-missing)
       (utils/shuffle-and-track-first)))

(defn generate [level]
  (let [eqs (generate-equations (inc level))
        [correct choices] (get-choices eqs)
        eq-to-cljml (fn [[base mod]]
                      [:mrow
                       [:mi "x"]
                       [:mo "&#8801;"]
                       [:mn base]
                       [:mo " "]
                       [:mtext (format "(mod %d)" mod)]])]

    {:question (concat [:mtable {:columnalign "left"}] (map eq-to-cljml eqs))
     :choices (map (fn [x] [:mn x]) choices)
     :correct correct}))

(def info {:type :congruence
           :name "Congruence relation"
           :description (str "Solve system of linear congruences.\n"
                             "http://en.wikipedia.org/wiki/Modular_arithmetic")
           :generator generate})

