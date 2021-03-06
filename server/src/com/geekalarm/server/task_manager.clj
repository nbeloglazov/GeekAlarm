(ns com.geekalarm.server.task-manager
  (:require [com.geekalarm.server.render-utils :as render]
            [clojure.java.io :as io]
            [clojure.string :as string]))


(defn tasks-folder []
  (->> "com/geekalarm/server/server.clj"
       io/resource
       io/as-file
       (.getParent)
       (#(str % "/tasks"))))


(defn get-namespaces [folder]
  (->> (io/as-file folder)
       file-seq
       (map #(.getName %))
       (filter #(re-matches #".*\.clj" %))
       (map #(re-find #"[^.]+" %))
       (map #(str "com.geekalarm.server.tasks." %))
       (map #(string/replace % "_" "-"))
       (map symbol)))

(defn resolve-task [ns]
  (require ns)
  (deref (ns-resolve ns 'info)))

(def tasks (->> (tasks-folder)
                get-namespaces
                (map resolve-task)
                (reduce #(assoc % (:type %2) %2) {})))

(defn generate-task [type level]
  (let [task ((get-in tasks [type :generator]) level)]
    (render/render-task task)))

(defn tasks-info []
  (->> (vals tasks)
       (map #(dissoc % :generator))
       (sort-by :name)))


