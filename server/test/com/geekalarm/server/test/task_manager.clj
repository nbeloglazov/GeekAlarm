(ns com.geekalarm.server.test.task-manager
  (:use clojure.test
        com.geekalarm.server.task-manager))


(defn valid-info? [info]
  (= #{:type :name :description}
     (set (keys info))))

(deftest test-tasks-info
  (doseq [info (tasks-info)]
    (is (valid-info? info))))

(defn valid-task? [{:keys [question choices correct]}]
  (and (not (nil? question))
       (= 4 (count choices))
       (every? #(not (nil? %)) choices)
       (contains? #{0 1 2 3} correct)))


(deftest test-generate-task
  (doseq [type (map :type (tasks-info))
          level [0 1 2]]
    (is (valid-task? (generate-task type level)))))


