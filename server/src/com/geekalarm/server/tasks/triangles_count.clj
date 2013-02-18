(ns com.geekalarm.server.tasks.triangles-count
  (:require [clojure.math.combinatorics :refer [combinations]]
            [incanter.core :refer (abs)]
            [com.geekalarm.server
             [render-utils :refer (image-to-stream)]
             [utils :refer (get-similar-by-one)]]))

(def size 330)
(def text-height 30)
(def offset 20)
(def levels [[4 5]
             [5 6]
             [6 7]])

(defn far-enough? [[[x0 y0] [x1 y1]]]
  (> (max (Math/abs (- x0 x1))
          (Math/abs (- y0 y1)))
     1))

(defn rand-point []
  [(rand-int size) (rand-int size)])

(defn enlarge-line [[[x0 y0] [x1 y1]]]
  (let [dx (- x0 x1)
        dy (- y0 y1)
        c 2]
    [[(+ x1 (* dx size c))
      (+ y1 (* dy size c))]
     [(- x0 (* dx size c))
      (- y0 (* dy size c))]]))

(defn rand-line []
  (->> (repeatedly #(list (rand-point) (rand-point)))
       (filter far-enough?)
       first
       enlarge-line))

(defn intersects? [[[x1 y1] [x2 y2]] [[x3 y3] [x4 y4]]]
  (java.awt.geom.Line2D/linesIntersect x1 y1 x2 y2 x3 y3 x4 y4))

(defn intersection [[[x1 y1] [x2 y2]] [[x3 y3] [x4 y4]]]
  (let [det (- (* (- x1 x2) (- y3 y4))
               (* (- y1 y2) (- x3 x4)))]
    (when-not (zero? det)
      (map #(/ % det)
           [(- (* (- (* x1 y2) (* y1 x2))
                  (- x3 x4))
               (* (- x1 x2)
                  (- (* x3 y4) (* y3 x4))))
            (- (* (- (* x1 y2) (* y1 x2))
                  (- y3 y4))
               (* (- y1 y2)
                  (- (* x3 y4) (* y3 x4))))]))))

(defn inside-offsets? [[x y] offset]
  (and (>= x offset)
       (>= y offset)
       (<= x (- size offset))
       (<= y (- size offset))))

(defn near-border? [p]
  (and (inside-offsets? p (- offset))
       (not (inside-offsets? p offset))))

(defn triangle-area [[l1 l2 l3]]
  (let [[x0 y0] (intersection l1 l2)
        [x1 y1] (intersection l2 l3)
        [x2 y2] (intersection l1 l3)]
    (/ (abs (- (* (- x0 x2)
                  (- y1 y0))
               (* (- x0 x1)
                  (- y2 y0))))
       2)))

(defn satisfy?
  ([line1 line2]
     (if-let [inters (intersection line1 line2)]
       (not (near-border? inters))
       false))
  ([lines]
     (and (every? true? (map #(apply satisfy? %) (combinations lines 2)))
          (every? #(> % 50) (map triangle-area (combinations lines 3))))))

(defn triangle-inside? [[l1 l2 l3]]
  (and (inside-offsets? (intersection l1 l2) 0)
       (inside-offsets? (intersection l2 l3) 0)
       (inside-offsets? (intersection l1 l3) 0)))

(defn calc-triangles [lines]
  (->> (combinations lines 3)
       (filter triangle-inside?)
       count))

(defn rand-lines [n]
  (->> (repeatedly #(repeatedly n rand-line))
       (filter satisfy?)
       first))

(defn draw-line [gr [[x0 y0] [x1 y1]]]
  (.drawLine gr x0 y0 x1 y1))

(defn draw-lines [gr lines]
  (doseq [line lines]
    (draw-line gr line)))

(defn get-image [lines]
  (let [image (java.awt.image.BufferedImage. size (+ text-height size) java.awt.image.BufferedImage/TYPE_INT_ARGB)
        gr (.getGraphics image)]
    (let [font (.. gr getFont (deriveFont (float 20)))]
      (.setFont gr font))
    (.setColor gr java.awt.Color/BLACK)
    (.drawString gr "How many triangles are there?" 10 20)
    (.setClip gr 0 text-height size size)
    (.translate gr 0 text-height)
    (draw-lines gr lines)
    image))

(defn generate [level]
  (let [n (rand-nth (levels level))
        lines (rand-lines n)
        image (get-image lines)
        [correct choices] (get-similar-by-one (calc-triangles lines))]
    {:question (image-to-stream image)
     :choices (map str choices)
     :correct correct}))

(def info {:type :triangles-count
           :name "Triangles count"
           :description (str "Calculate how many triangles are on screen.")
           :generator #'generate})
