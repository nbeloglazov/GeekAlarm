(ns com.geekalarm.server.latex-utils
  (:require [clojure.string :refer [join]]
            [clojure.math.numeric-tower :as math]
            [incanter.core :as i]))

(defn lines
  ([lines align]
     (str "\\begin{array}{" align "}"
       (join "\\\\" lines)
       "\\end{array}"))
  ([lns]
     (lines lns "l")))

(defn pow [base pow]
  (format "%s^{%s}" base pow))

(defn escape [text]
  (->> (map str text)
       (replace {"{" "\\{"
                 "}" "\\}"
                 "\\" "{\\backslash}"})
       (apply str)))

(defn text [text]
  (format "\\text{%s}" (escape text)))

(defn- render-regular-fraction [num]
  (format "%s\\frac{%s}{%s}"
          (if (pos? num) "" "-")
          (Math/abs (long (numerator num)))
          (denominator num)))

(defn- render-mixed-fraction [num]
  (let [sgn (if (neg? num) -1 1)
        fr (* num sgn)
        q (quot (numerator fr) (denominator fr))
        r (- fr q)]
    (if (zero? q)
      (render-regular-fraction (* sgn r))
      (str (* sgn q) (render-regular-fraction r)))))

(defn to-latex
  ([num]
   (to-latex num false))
  ([num use-mixed-fraction?]
   (cond (ratio? num) (if use-mixed-fraction?
                        (render-mixed-fraction num)
                        (render-regular-fraction num))
         (number? num) (str (math/round num))
         :default (str num))))

(defn matrix [matrix type]
  (let [open {"()" "\\left("
              "||" "\\left|"}
        close {"()" "\\right)"
               "||" "\\right|"}]
    (str (open type)
         "\\begin{array}{" (apply str (repeat (i/length (first matrix)) "r")) "}"
         (join "\\\\" (map #(join "&" (map to-latex %)) matrix))
         "\\end{array}"
         (close type))))





