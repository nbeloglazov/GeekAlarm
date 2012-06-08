(ns com.geekalarm.server.task-manager
  (:require [com.geekalarm.server.render-utils :as render]
            [clojure.java.io :as io]
            [clojure.string :as string]))


(defn generators-folder []
  (->> "com/geekalarm/server/server.clj"
       io/resource
       io/as-file
       (.getParent)
       (#(str % "/generators"))))


(defn get-namespaces [folder]
  (->> (io/as-file folder)
       file-seq
       (map #(.getName %))
       (filter #(re-matches #".*\.clj" %))
       (map #(re-find #"[^.]+" %))
       (map #(str "com.geekalarm.server.generators." %))
       (map #(string/replace % "_" "-"))
       (map symbol)))

(defn resolve-generator [ns]
  (require ns)
  (let [[cat gen] (map #(deref (ns-resolve ns %)) '[category generate])]
    (vary-meta gen assoc :category cat)))

(defn get-generators [namespaces]
  (group-by #(:category (meta %)) (map resolve-generator namespaces)))

(def generators (get-generators (get-namespaces (generators-folder))))

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

