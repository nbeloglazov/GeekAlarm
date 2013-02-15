(ns com.geekalarm.server.tasks.max-matching
  (:require [com.geekalarm.server
             [utils :refer (rand-range get-similar-by-one)]
             [render-utils :refer (image-to-stream)]]))

(def levels [[2 3]
             [4 5]
             [6 7]])

(def radius 6)
(def diam (* 2 radius))

(def text-offset 25)
(def gap 30)

(def width 170)


(defn generate-graph [level]
  (let [left-n (rand-nth (levels level))
        right-n (rand-nth (levels level))
        max (+ left-n right-n)
        n (->> [0.7 1.5]
               (map #(* max %))
               (map #(Math/floor %))
               (map int)
               (apply rand-range))]
    (->> (for [left (range left-n)
               right (range right-n)]
           [left right])
         shuffle
         (take n)
         (group-by first)
         (map (fn [[key val]] [key (map second val)]))
         (into {})
         (#(with-meta % {:left left-n
                         :right right-n
                         })))))


(defn find-chain [graph from visited v]
  (loop [visited visited
         neibs (graph v)]
    (if (empty? neibs)
      {:result nil
       :visited visited}
      (let [neib (first neibs)]
        (if (visited neib)
          (recur visited (rest neibs))
          (if (contains? (set (keys from)) neib)
            (let [{:keys [visited result]} (find-chain graph from (conj visited neib) (from neib))]
              (if (nil? result)
                (recur visited (rest neibs))
                {:result (concat [v neib] result)
                 :visited visited}))
            {:result [v neib]
             :visited visited}))))))

(defn add-chain [chain from]
  (->> (partition 2 chain)
       (map reverse)
       (reduce #(apply assoc % %2) from)))


(defn max-matching [graph]
  (loop [from {}
         v (keys graph)]
    (if (empty? v)
      from
      (if-let [chain (->> (first v)
                          (find-chain graph from #{})
                          :result)]
        (recur (add-chain chain from) (rest v))
        (recur from (rest v))))))

(defn save-image [image]
  (javax.imageio.ImageIO/write image "PNG" (java.io.File. "/home/nikelandjelo/res.png")))

(defn draw-point [gr [x y]]
  (.fillOval gr (- (int x) radius) (- (int y) radius) diam diam))

(defn draw-line [gr [x1 y1] [x2 y2]]
  (.drawLine gr (int x1) (int y1) (int x2) (int y2)))

(defn draw-graph [graphics graph coords-l coords-r]
  (let [left (->> graph meta :left range)
        right (->> graph meta :right range)]
    (.setColor graphics java.awt.Color/BLACK)
    (doseq [point (concat (map coords-l left)
                          (map coords-r right))]
            (draw-point graphics point))
    (doseq [l left]
      (doseq [r (graph l [])]
        (draw-line graphics
                   (coords-l l)
                   (coords-r r))))))


(defn get-y [height n ind]
  (-> height
      (- radius radius text-offset)
      (/ (dec n))
      (* ind)
      (+ radius text-offset)))

(defn draw-question-string [gr]
  (.drawString gr "Size of maximum matching?" 5 15))

(defn draw-question [graph]
  (let [im-width (+ width radius radius)
        size-l (->> graph meta :left)
        size-r (->> graph meta :right)
        height (->> (max size-l size-r) dec (* gap) (+ radius radius text-offset))
        image (java.awt.image.BufferedImage. im-width height java.awt.image.BufferedImage/TYPE_INT_ARGB)
        graphics (.createGraphics image)]
    (doto graphics
      (.setColor java.awt.Color/WHITE)
      (.fillRect 0 0 im-width height)
      (draw-graph graph
                  #(vector radius (get-y height size-l %))
                  #(vector (+ radius width) (get-y height size-r %)))
      draw-question-string)
    image))


(defn generate [level]
  (let [graph (generate-graph level)
        [correct choices] (->> graph max-matching count get-similar-by-one)]
    {:question (image-to-stream (draw-question graph))
     :correct correct
     :choices (map str choices)}))

(def info {:type :max-matching
           :name "Maximum matching"
           :description (str "Find size of maximium matching for given graph.\n"
                             "http://en.wikipedia.org/wiki/Matching_(graph_theory)")
           :generator #'generate})


