(ns com.geekalarm.server.tasks.unique-substrings
  (:require [com.geekalarm.server
             [utils :refer (get-similar-by-one)]
             [latex-utils :refer (text lines)]]))

(def levels [ {:letters [2 3] :length [3 4]}
              {:letters [2 3] :length [4 5]}
              {:letters [3 4] :length [5 6]}])

(defn generate-alphabet [size]
  (->> (range 26)
       (map #(+ (int \a) %))
       (map char)
       shuffle
       (take size)))

(defn generate-string [length alphabet]
  (->> #(rand-nth alphabet) (repeatedly length) (apply str)))

(defn unique-substrings [str]
  (->> (for [s (range (count str))
             e (range (inc s) (inc (count str)))]
         (subs str s e))
       set
       count))

(defn generate [level]
  (let [{:keys [length letters]} (levels level)
        length (rand-nth length)
        letters (rand-nth letters)
        st (generate-string length (generate-alphabet letters))
        result (unique-substrings st)
        [correct choices] (get-similar-by-one result)]
    {:question (lines (map text ["Number of unique substrings of the string:" (str \' st \')]) "c")
     :correct correct
     :choices (map str choices)}))


(def info
  {:type :unique-substrings
   :name "Unique substrings"
   :generator #'generate
   :description (str "Calculate number of unique substrings of the given string.\n"
                     "Example: \"abab\". Substrings: \"a\", \"b\", \"ab\", \"ba\", \"aba\", \"bab\", \"abab\". Answer: 7")})