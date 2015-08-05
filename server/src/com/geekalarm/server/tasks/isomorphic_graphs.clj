(ns com.geekalarm.server.tasks.isomorphic-graphs
  (:require [clojure.math.combinatorics :as comb]
            [incanter.core :as ic]
            [com.geekalarm.server
             [utils :as u]
             [latex-utils :as lu]
             [render-utils :as ru]]))

(def size 300)
(def radius 5)
(def offset 10)
(def min-dist 30)
(def min-angle (Math/toRadians 5))
(def levels [[[3 1] [3 2] [4 1] [4 2]]
             [[4 3] [4 4] [4 5]]
             [[5 4] [5 5] [5 6] [5 7] [5 8] [6 5] [6 6] [6 7]]])

(defn rand-graph [n m]
  {:n n
   :edges (->> (comb/combinations (range n) 2)
               (map set)
               shuffle
               (take m)
               set)} )

(defn rand-point []
  (letfn [(rand-coord [] (+ offset (rand-int (- size offset offset))))]
   [(rand-coord) (rand-coord)]))

(defn vertices-positions [{n :n}]
  (into {} (for [i (range n)] [i (rand-point)])))

(defn draw-vertices [gr {n :n} positions]
  (doseq [vert (range n)]
    (let [[x y] (positions vert)]
      (.fillOval gr (- x radius) (- y radius) (* 2 radius) (* 2 radius)))))

(defn add-rand-edge [{:keys [edges n] :as graph}]
  (->> (comb/combinations (range n) 2)
       shuffle
       (remove edges)
       first
       set
       (update-in graph [:edges] conj)))

(defn remove-rand-edge [{:keys [edges] :as graph}]
  (->> (seq edges)
       rand-nth
       (update-in graph [:edges] disj)))

(defn rand-modify [graph]
  (let [modify (rand-nth [add-rand-edge
                                remove-rand-edge
                                (comp add-rand-edge remove-rand-edge)])]
    (modify graph)))

(defn degree-sequence [{:keys [edges]}]
  (->> (apply concat edges)
       frequencies
       vals
       sort))

(defn equals-by-degree? [gr1 gr2]
  (= (degree-sequence gr1) (degree-sequence gr2)))

(defn gen-different [graph]
  (u/find-matching-value
   #(rand-modify graph)
   #(not (equals-by-degree? graph %))))

(defn angle [p0 p1 p2]
  (let [v1 (ic/minus p1 p0)
        v2 (ic/minus p2 p0)
        len #(ic/sqrt (ic/sum-of-squares %))]
    (ic/acos (ic/abs (/ (ic/sum (ic/mult v1 v2))
                        (len v1)
                        (len v2))))))

(defn no-parallel-like-edges? [{:keys [n edges]} positions]
  (every? true? (for [v0 (range n)
                      edge1 edges :when (edge1 v0)
                      edge2 edges :when (and (edge2 v0)
                                             (not= edge1 edge2))]
                  (let [v1 (first (disj edge1 v0))
                        v2 (first (disj edge2 v0))
                        angle (angle (positions v0) (positions v1) (positions v2))]
                    (>= angle min-angle)))))

(defn vertices-far-enough? [points]
  (letfn [(dist [[[x0 y0] [x1 y1]]] (Math/hypot (- x0 x1) (- y0 y1)))]
    (->> (comb/combinations points 2)
         (map dist)
         (apply min)
         (<= min-dist))))

(defn cover-free-space? [points]
  (let [xs (map first points)
        ys (map second points)
        width (fn [coll] (- (reduce max coll) (reduce min coll)))]
    (and (> (width xs) (* 2/3 size))
         (> (width ys) (* 2/3 size)))))

(defn good-positions? [graph positions]
  (let [points (vals positions)]
    (and (cover-free-space? points)
         (vertices-far-enough? points)
         (no-parallel-like-edges? graph positions))))

(defn draw-edges [gr {edges :edges} positions]
  (doseq [[a b] (map seq edges)]
    (let [[x-a y-a] (positions a)
          [x-b y-b] (positions b)]
      (.drawLine gr x-a y-a x-b y-b))))

(defn draw-graph-randomly [graph]
  (let [positions (u/find-matching-value #(vertices-positions graph)
                                         #(good-positions? graph %))
        image (java.awt.image.BufferedImage. size size java.awt.image.BufferedImage/TYPE_INT_ARGB)
        gr (.getGraphics image)]
    (.setColor gr java.awt.Color/BLACK)
    (draw-vertices gr graph positions)
    (draw-edges gr graph positions)
    image))


(defn generate [level]
  (let [[n m] (rand-nth (levels level))
        graph (rand-graph n m)
        non-isom (gen-different graph)
        [correct choices] (u/shuffle-and-track-first [non-isom graph graph graph])]
    {:question (lu/lines (map lu/text ["One graph is not isomorphic to the other three."
                                       "Which one?"])
                            "c")
     :choices (map ru/image-to-stream (map draw-graph-randomly choices))
     :correct correct}))

(def info {:type :isomorphic-graphs
           :name "Isomorphic graphs"
           :description (str "Find non-isomorphic graph.\n"
                             "http://en.wikipedia.org/wiki/Graph_isomorphism")
           :generator #'generate})
