(ns com.geekalarm.server.tasks.lucky-tickets
  (:use [com.geekalarm.server.utils :only (shuffle-and-track-first)]))

(def levels [[2 3]
             [4 5 6]
             [7 8 9]])

(defn generate-half [size sum]
  (let [num (vec (repeat size 0))
        inc-rand (fn [digits]
                   (->> (repeatedly #(rand-int size))
                        (filter #(< (digits %) 9))
                        first
                        (#(update-in digits [%] inc))))]
    (nth (iterate inc-rand num) sum)))


(defn generate-number [size]
  (let [sum (rand-int (inc (* 9 size)))]
    (vec (concat (generate-half size sum) (generate-half size sum)))))

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

(defn generate [level]
  (let [number (->> level levels rand-nth generate-number)
        [correct choices] (similar number)]
    {:question [:mtext "Lucky ticket?"]
     :choices (map (fn [x] [:mn (apply str x)]) choices)
     :correct correct}))

(def info {:type :lucky-tickets
           :name "Lucky tickets"
           :description (str "Ticket is lucky if it's number has 2n digits and"
                             "sum of the first n digits equals to sum of the last n digits."
                             "Example: 2314, 22, 123600")
           :generator generate})