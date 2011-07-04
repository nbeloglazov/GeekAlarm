(ns nbeloglazov.geekalarm.server.utils
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

(defn get-similar-by-one [x]
  (let [r (rand-int 4)
	a (- x r)]
    [(inc r) (range a (+ a 4))]))
