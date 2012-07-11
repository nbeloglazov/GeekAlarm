(ns com.geekalarm.server.tasks.lucky-tickets
  (:use [com.geekalarm.server.utils :only (shuffle-and-track-first)]))

(def levels [[2 3]
             [4 5 6]
             [7 8 9]])

(def variants
  (memoize (fn [sum length]
             (cond (and (zero? length) (zero? sum))
                   1
                   (or (neg? sum) (> sum (* 9 length)))
                   0
                   :else
                   (->> (range 10)
                        (map #(variants (- sum %) (dec length)))
                        (apply +))))))

(defn rand-with-weight [weights]
  (let [rnd (rand)
        sum (apply + (vals weights))]
    (->> (seq  weights)
         (reductions (fn [[val sum-weight] [new-val weight]]
                       (vector new-val (+ sum-weight (/ weight sum))))
                     [nil 0])
         (filter (fn [[val weight]] (> weight rnd)))
         first
         first)))

(defn rand-digit [sum length]
  (->> (range 10)
       (reduce #(assoc % %2
                       (variants (- sum %2) (dec length))) {})
       rand-with-weight))

(defn rand-number [sum length]
  (->> [[] sum length]
       (iterate (fn [[acc sum length]]
                  (let [dig (rand-digit sum length)]
                    [(conj acc dig) (- sum dig) (dec length)])))
       (drop-while #(pos? (last %)))
       first
       first))

(defn generate-number [size]
  (let [sum (rand-int (inc (* 9 size)))]
    (vec (concat (rand-number sum size) (rand-number sum size)))))

(defn similar-digit-fn [digit]
  (cond (zero? digit) inc
        (= 9 digit) dec
        :else (rand-nth [inc dec])))

(defn similar [number]
  (let [size (count number)
        indexes (->> #((juxt rand-int rand-int) size)
                         repeatedly
                         (remove #(= (first %) (last %)))
                         first)
        [fn1 fn2] (->> indexes (map number) (map similar-digit-fn))
        numbers (->> [[identity identity]
                      [identity fn2]
                      [fn1 identity]
                      [fn1 fn2]]
                     (map (fn [fns] (map #(vector % %2) indexes fns)))
                     (map #(reduce (fn [num [ind fn]] (update-in num [ind] fn)) number %)))]
    (shuffle-and-track-first numbers)))

(defn lucky? [number]
  (let [length (count number)]
    (= (apply + (take (/ length 2) number))
       (apply + (drop (/ length 2) number)))))


(defn satisfy?
  "Checks whether only 1 of the number is lucky ticket"
  [[_ numbers]]
  (->> numbers
       (filter lucky?)
       count
       (= 1)))


(defn generate [level]
  (let [number (->> level levels rand-nth generate-number)
        [correct choices] (->> #(similar number) repeatedly (filter satisfy?) first)]
    {:question [:mtext "Lucky ticket?"]
     :choices (map (fn [x] [:mn (apply str x)]) choices)
     :correct correct}))

(def info {:type :lucky-tickets
           :name "Lucky tickets"
           :description (str "Ticket is lucky if it's number has 2n digits and\n"
                             "sum of the first n digits equals to sum of the last n digits.\n"
                             "Examples: 2314, 22.")
           :generator generate})