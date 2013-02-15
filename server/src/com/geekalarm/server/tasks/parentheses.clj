(ns com.geekalarm.server.tasks.parentheses
  (:require [com.geekalarm.server
             [utils :refer (get-similar-by-one)]
             [latex-utils :refer (lines to-latex text)]]))

(def levels [[3] [4] [5 6]])

(defn generate-ops [length]
  (repeatedly (dec length) #(rand-nth '[+ - *])))

(defn merge-with-op [op vals1 vals2]
  (set (for [v1 (seq vals1) v2 (seq vals2)]
         ((resolve op) v1 v2))))

(declare split)

(defn calculate [nums ops]
  (if (empty? ops)
    (set nums)
    (->> (count ops)
         range
         (map #(split nums ops %))
         (apply concat)
         set)))

(defn split [nums ops ind]
  (let [[nums-l nums-r] (split-at (inc ind) nums)
        [ops-l ops-r] (split-at ind ops)
        vals-l (calculate nums-l ops-l)
        vals-r (calculate nums-r (rest ops-r))]
    (merge-with-op (nth ops ind) vals-l vals-r)))

(defn generate [level]
  (let [len (rand-nth (levels level))
        ops (generate-ops len)
        nums (repeatedly len #(rand-int 10))
        [correct choices] (-> (calculate nums ops) count get-similar-by-one)]
    {:question (lines (concat (map text ["How many different values can be obtained"
                                         "from this expression via adding"
                                         "arbitrary number of parentheses:"])
                              [(apply str (interleave nums (concat ops [""])))]) 
                      "c")
     :correct correct
     :choices (map to-latex choices)}))

(def info
  {:type :parentheses
   :name "Parentheses"
   :generator #'generate
   :description (str "Example: 2+2*2 can be converted to (2+2)*2 and 2+(2*2).\n"
                     "It gives 2 different results: 8 and 6. Answer is 2.")})