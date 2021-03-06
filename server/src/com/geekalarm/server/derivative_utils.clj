(ns com.geekalarm.server.derivative-utils
  (:require [com.geekalarm.server.latex-utils :as lu]
            [clojure.core.match :refer (match)]
            [clojure.string :refer (join)]))

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
                                 {nums true  exprs false} (group-by number? args)
                                 num (reduce + nums)]
                             (->> (if (zero? num) exprs (cons num exprs))
                                  (#(cond (empty? %) 0
                                          (= (count %) 1) (first %)
                                          :else (cons :plus %)))))
          [[:minus arg]] (cond (= arg 0) 0
                               (fn-of arg :minus) (last arg)
                               :else expr)
          [[:mult & args]] (let [{:keys [args minus]} (flatten-mult args)
                                 {nums true exprs false} (group-by number? args)
                                 num (reduce * nums)]
                             (if (zero? num)
                               0
                               (->> (if (= num 1) exprs (cons num exprs))
                                    (#(if (= (count %) 1)
                                        (first %)
                                        (cons :mult %)))
                                    (#(if minus [:minus %] %)))))
          :else expr)))

(defmulti to-latex-expr (fn [[f]] (if (fns f) :function f)))

(defn to-latex [expr]
  (cond (number? expr) (lu/to-latex expr)
        (= expr :x) "x"
        (fns expr) (str "\\" (name expr))
        :else (to-latex-expr expr)))

(defmethod to-latex-expr :pow
  [[_ arg pow]]
  (let [l-pow (to-latex pow)
        l-arg (to-latex arg)
        [fn in-arg] (when (coll? arg) arg)]
    (cond (fns fn) (str (lu/pow (to-latex fn) l-pow) "(" (to-latex in-arg) ")")
          (= l-arg "x") (lu/pow l-arg l-pow)
          :default (lu/pow (str "(" l-arg ")") l-pow))))

(defmethod to-latex-expr :function
  [[f arg]]
  (str (to-latex f) "(" (to-latex arg) ")"))

(defmethod to-latex-expr :exp
  [[_ base pow]]
  (lu/pow (to-latex base) (to-latex pow)))

(defmethod to-latex-expr :plus
  [[_ & args]]
  (let [ops (map #(if (fn-of % :minus) "" "+") args)
        l-args (map to-latex args)]
    (->> (interleave (cons "" (rest ops))
                     l-args)
         (remove empty?)
         (apply str))))

(defmethod to-latex-expr :minus
  [[_ arg]]
  (str "-" (to-latex arg)))

(defmethod to-latex-expr :mult
  [[_ & args]]
  (letfn [(conf [v]
            (if (fn-of v :plus)
              (str "(" (to-latex v) ")")
              (to-latex v)))]
    (apply str (map conf args))))

(defmethod to-latex-expr :div
  [[_ num den]]
  (lu/frac (to-latex num) (to-latex den)))
