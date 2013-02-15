(ns com.geekalarm.server.latex-utils
  (:require [clojure.string :refer (join)]
            [clojure.math.numeric-tower :as math]))

(defn lines
  ([lines align]
     (str "\\begin{array}{" align "}"
       (join "\\\\" lines)
       "\\end{array}"))
  ([lns]
     (lines lns "l")))

(defn frac [num den]
  (format "\\frac{%s}{%s}" num den))

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

(defn to-latex [num]
  (cond (ratio? num) (frac (numerator num) (denominator num))
        (number? num) (str (math/round num))
        :default (str num)))

(defn matrix [matrix type]
  (let [open {"()" "\\left("
              "||" "\\left|"}
        close {"()" "\\right)"
               "||" "\\right|"}]
    (str (open type)
         "\\begin{array}{" (apply str (repeat (count (first matrix)) "r")) "}"
         (join "\\\\" (map #(join "&" (map to-latex %)) matrix))
         "\\end{array}"
         (close type))))





