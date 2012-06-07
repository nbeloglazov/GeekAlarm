(ns com.geekalarm.server.task-manager
  (:require [com.geekalarm.server.render-utils :as render]
            [clojure.java.io :as io]))

(def generators-folder "com/geekalarm/server/generators")

(defn get-namespaces [folder]
  (->> folder
       io/resource
       io/as-file
       file-seq
       (map #(.getName %))
       (filter #(re-matches #".*\.clj" %))
       (map #(re-find #"[^.]+" %))
       (map #(str folder "/" %))
       (map #(replace {\_ \- \/ \.} %))
       (map #(apply str %))
       (map symbol)))

(defn resolve-generator [ns]
  (require ns)
  (let [[cat gen] (map #(deref (ns-resolve ns %)) '[category generate])]
    (vary-meta gen assoc :category cat)))

(defn get-generators [namespaces]
  (group-by #(:category (meta %)) (map resolve-generator namespaces)))

(def generators (get-generators (get-namespaces generators-folder)))

(def description
     {:linear-algebra {:name "Linear algebra"}
      :math-analysis {:name "Mathematical analysis"}
      :computer-science {:name "Computer science"}
      :number-theory {:name "Number theory"}})

(defn get-task [category level]
  (let [task ((rand-nth (generators category)) level)]
    (if (coll? (:question task))
      (render/render-cljml-task task)
      task)))

(defn get-categories []
  (map (fn [categ]
	 {:code (name categ)
	  :name (:name (description categ))})
       (keys description)))

