(ns com.geekalarm.server.utils
  (:require [incanter.core :as incanter]
            [clojure.contrib
             [seq-utils :as seq-utils]])
  (:import [java.util Timer TimerTask]))

(def *running-timers* (atom []))

(defn start-timer [fn  time]
  (let [timer (Timer.)
	task (proxy [TimerTask] []
	       (run [] (fn)))]
    (.schedule timer task (long 0) (long time))
    (swap! *running-timers* conj timer)
    timer))

(defn stop-timers []
  (doseq [timer @*running-timers*]
    (.cancel timer))
  (reset! *running-timers* []))

(defn shuffle-and-track-first [seq]
        (let [frst (first seq)
              shuffled (->> (shuffle seq)
                            (seq-utils/indexed))]
          [(first (seq-utils/find-first #(= frst (second %)) shuffled))
           (map second shuffled)]))

(defn get-similar-by-one [x]
  (let [r (rand-int 4)
	a (- x r)]
    [(inc r) (range a (+ a 4))]))

(defn random-matrix [size max]
  (let [max (inc max)
        rnd #(- (rand-int (* 2 max)) max)]
  (-> (repeatedly (* size size) rnd)
      (incanter/matrix size))))

(defn get-similar-matrices [matrix]
  (let [mat (if (incanter/matrix? matrix)
              (incanter/to-vect matrix)
              (vec (map vec matrix)))
        n (count mat)
        pos (->> (range (* n n))
                 (shuffle)
                 (take 2)
                 (vec))
        difs [(dec (* 2 (rand-int 2)))
              (dec (* 2 (rand-int 2)))]
        types [[0 0] [0 1] [1 0] [1 1]]
        update-matrix (fn [pos dif mat]
                        (update-in mat [(quot pos n)
                                        (rem pos n)]
                                   + dif))
        create-matrix (fn [type]
                        (reduce #(update-matrix
                                  (pos %2)
                                  (* (type %2) (difs %2))
                                  %1)
                                mat
                                [0 1]))
        [correct order] (shuffle-and-track-first (range 4))]
    [(inc correct)
     (map create-matrix (map types order))]))

(defn get-similar-by-fn [orig similar-fn]
  (let [sim-seq (repeatedly #(similar-fn orig))
        add-sim (fn [found]
                  (->> sim-seq
                       (seq-utils/find-first #(not (seq-utils/includes? found %)))
                       (conj found)))]
    (-> (iterate add-sim [orig])
        (nth 3)
        (shuffle-and-track-first)
        (vec)
        (update-in [0] inc))))

    



