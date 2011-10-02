(ns com.geekalarm.server.derivative-utils
  (:use [clojure.contrib.seq-utils :only (separate)]
        [com.geekalarm.server.mathml-utils :only (cljml)]
        [clojure.core.match.core :only (match)]))

(declare derivative-expr)

(def fns #{:sin :cos :ln :tan})

(defn fn-of [expr type]
  (and (coll? expr)
       (= type (first expr))))

(defn derivative [expr]
  (cond (number? expr) 0
        (= expr :x) 1
        :else (derivative-expr expr)))

(defn derivative-f [func arg]
  (->> (case func
             :sin [:cos arg]
             :cos [:minus [:sin arg]]
             :tan [:div 1 [:pow [:cos arg] 2]]
             :ln  [:div 1 arg])
       (conj [:mult (derivative arg)])))

(defn derivative-expr [expr]
  (match [(vec expr)]
         [[:pow arg pow]] [:mult pow
                           [:pow arg
                            (if (fn-of pow :minus)
                              [:minus (inc (last pow))]
                              (dec pow))]
                           (derivative arg)]
         [[:exp base pow]] [:mult
                          [:ln base]
                          [:exp base pow]
                          (derivative pow)]
         [[:plus & args]] (->> (map derivative args)
                             (cons :plus))
         [[:minus & args]] [:minus (derivative (first args))]
         [[:mult a & rst]] (if (empty? rst)
                             (derivative a)
                             [:plus [:mult (derivative a)
                                     (cons :mult rst)]
                              [:mult a
                               (derivative (cons :mult rst))]])
        [[:div num den]] [:div [:plus [:mult (derivative num)
                                     den]
                                [:minus [:mult num
                                         (derivative den)]]]
                          [:pow den 2]]
        [[func arg]] (derivative-f func arg)))

(declare normalize-expr)

(defn normalize [expr]
  (cond (number? expr) (if (neg? expr) [:minus (- expr)] expr)
        (= expr :x) :x
        :else (normalize-expr expr)))

(defn flatten-mult [args]
  (let [minus (->> (filter coll? args)
                   (filter #(= (first %) :minus))
                   (count)
                   (odd?))
        args (map #(if (and (coll? %)
                            (= (first %) :minus))
                     (second %)
                     %) args)
        red-fun (fn [state arg]
                  (if (fn-of arg :mult)
                    (let [{:keys [minus args]} (flatten-mult (rest arg))]
                      (-> state
                          (update-in [:minus] not= minus)
                          (update-in [:args] concat args)))
                    (update-in state [:args] conj arg)))]
    (reduce red-fun {:args [] :minus minus} args)))
                          
(defn normalize-expr [[func & args]]
  (let [args (map normalize args)
        expr (cons func args)]
    (match [(vec expr)]
          [[:pow arg pow]] (let [[arg pow] (if (fn-of arg :pow)
                                             [(second arg) (* (last arg) pow)]
                                             [arg pow])]
                            (cond (= pow 1) arg
                                  (= pow 0) 1
                                  :else [:pow arg pow]))
          [[:exp & _]] expr
          [[:plus & args]] (let [args (reduce #(if (fn-of %2 :plus)
                                                 (concat %1 (rest %2))
                                                 (conj %1 %2)) [] args)
                                 [nums exprs] (separate number? args)
                                 num (reduce + nums)]
                             (->> (if (zero? num) exprs (cons num exprs))
                                  (#(cond (empty? %) 0
                                          (= (count %) 1) (first %)
                                          :else (cons :plus %)))))
          [[:minus arg]] (cond (= arg 0) 0
                               (fn-of arg :minus) (last arg)
                               :else expr)
          [[:mult & args]] (let [{:keys [args minus]} (flatten-mult args)
                                 [nums exprs] (separate number? args)
                                 num (reduce * nums)]
                             (if (zero? num)
                               0
                               (->> (if (= num 1) exprs (cons num exprs))
                                    (#(if (= (count %) 1)
                                        (first %)
                                        (cons :mult %)))
                                    (#(if minus [:minus %] %)))))
          :else expr)))

(declare to-cljml-expr)

(defn to-cljml [expr]
  (cond (number? expr) (cljml expr)
        (= expr :x) [:mi "x"]
        :else (vec (to-cljml-expr expr))))

(defn to-cljml-expr [[func & in-args]]
  (let [args (map to-cljml in-args)]
    (case func
          :pow (let [[arg pow] args
                     in-arg (first in-args)]
                 (cond (= arg [:mi "x"]) [:msup arg pow]
                       (and (coll? in-arg)
                            (fns (first in-arg)))
                       (update-in arg [1] (fn [op] [:msup op pow]))
                       :else [:msup
                              [:mfenced arg]
                              pow]))
          :exp (let [[base pow] args]
                 [:msup base pow])
          :plus (let [ops (map #(if (fn-of % :minus)
                                  ""
                                  [:mo "+"]) in-args)]
                  (->> (interleave (cons "" (rest ops))
                                   args)
                       (remove empty?)
                       (cons :mrow)))
          :minus [:mrow [:mo "-"] (first args)]
          :mult (->> (map #(if (fn-of %2 :plus) [:mfenced %1] %1) args in-args)
                     (cons :mrow))
          :div (cons :mfrac args)
          [:mrow
           [:mo (name func)]
           [:mfenced
           (first args)]])))


                                
                           
        
         


