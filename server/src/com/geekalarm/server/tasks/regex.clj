(ns com.geekalarm.server.tasks.regex
  (:import [dk.brics.automaton RegExp])
  (:require [com.geekalarm.server
             [utils :as utils]
             [mathml-utils :as cljml]]))

(def max-count 3)
(def max-length 30)

(defn rand-int-within [a b]
  (->> (- b a)
       (inc)
       (rand-int)
       (+ a)))

(defn rand-char
  ([]
     (->> [[\a \z] [\A \Z] [\0 \9]]
          (rand-nth)
          (apply rand-char)))
  ([from to]
     (->> [from to]
          (map int)
          (apply rand-int-within)
          (char)))
  ([transition]
     (->> [(.getMin transition) (.getMax transition)]
          (map int)
          (apply rand-int-within)
          (char))))


(defn one-char-exp []
  ((rand-nth [#(rand-char)
              #(str "\\" (rand-nth [\+ \. \? \* \( \) \\ \[ \]]
                                   ))])))

(defn range-char-exp []
  (->> [[\A \Z] [\a \z] [\0 \9]]
       (rand-nth)
       (partial apply rand-char)
       (repeatedly 2)
       (sort)
       (apply format "[%s-%s]")))

(defn char-exp []
  ((rand-nth [one-char-exp range-char-exp])))

(defn group-char-exp []
  (let [n (rand-int-within 1 2)
        delims (repeatedly n #(rand-nth ["" "|"]))]
    (->> (repeatedly n char-exp)
         (interleave delims)
         (cons (char-exp))
         (apply str)
         (#(format "(%s)" %)))))

(defn quantifier-exp []
  (let [rnd-count #(rand-int-within 1 (dec max-count))
        res (rand-nth
             ["*" "+" "?"
              (fn [] (format "{%d}" (rnd-count)))
              (fn [] (format "{%d,}" (rnd-count)))
              (fn [] (->> (repeatedly 2 rnd-count)
                          (sort)
                          (apply format "{%d,%d}")))])]
    (if (fn? res)
      (res) res)))

(def regex-fns [
                [#(apply str (repeatedly 3 char-exp))
                 #(str (char-exp) (quantifier-exp))]
                [#(->> (fn [] (str ((rand-nth [group-char-exp char-exp]))
                                   (quantifier-exp)))
                       (repeatedly 2)
                       (apply str))]
                [#(let [fn (first (second regex-fns))]
                    (str (fn) (fn)))]])

(defn generate-regex [level]
  (->> (nth regex-fns level)
       (rand-nth)
       (#(%))))

(defn available-transitions [state stats]
  (let [transitions (seq (.getSortedTransitions state true))]
    (if (.isAccept state)
      (conj transitions :accept)
      transitions)))

(defn generate-xeger [regex]
  (let [initial-state (.getInitialState regex)]
    (loop [state initial-state
           stats {}
           acc []]
      (let [transition (rand-nth (available-transitions state stats))]
        (if (= :accept transition)
          (apply str acc)
          (recur (.getDest transition)
                 (update-in stats [(.getDest transition)] #(if (nil? %) 0 (inc %)))
                 (conj acc (rand-char transition))))))))

(defn similar-string [string]
  (let [pos (rand-int (count string))]
    (apply str (assoc (vec string)
                 pos
                 (rand-char)))))

(defn similar-invalid [regex st]
  (->> (repeatedly 100 #(similar-string st))
       (filter #(not (.run regex %)))
       (first)
       (#(if (nil? %)
           "He$$o_world!"
           %))))

(defn generate [level]
  (let [regex (generate-regex level)
        automat (.toAutomaton (RegExp. regex))
        answer (generate-xeger automat)
        invalid (->> (repeatedly 3 #(generate-xeger automat))
                     (map #(similar-invalid automat %)))
        [correct choices] (utils/shuffle-and-track-first
                           (cons answer invalid))]
    (if (> (->> (map count choices)
                (reduce max))
           max-length)
      (recur level)
      {:question [:mtext regex]
       :choices (map (fn [x] [:mtext x]) choices)
       :correct correct})))

(def info {:type :regex
           :name "Regex"
           :description (str "Choose string matching the pattern.\n"
                             "http://www.regular-expressions.info/reference.html")
           :generator generate})


