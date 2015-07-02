(ns com.geekalarm.server.server
  (:import [java.util Date])
  (:require [net.cgrand.moustache :refer [app delegate]]
            [ring.middleware
             [params :refer [wrap-params]]
             [keyword-params :refer [wrap-keyword-params]]]
            [com.geekalarm.server
             [render-utils :as render]
             [mathml-utils :as mathml]
             [task-manager :as manager]
             [utils :as utils]]
            [clojure.data.json :as json]))


(def task-timeout (* 10 60 1000))

(def timer-interval (* 1 60 1000))

(def active-tasks (atom {}))

(defn remove-expired-tasks [tasks]
  (let [lower-bound (- (.getTime (Date.))
                       task-timeout)]
    (->> tasks
         (filter (fn [[id task]]
                   (>= (:timestamp task) lower-bound)))
         (into {}))))

(defn run-collector []
  (println "Collector started")
  (utils/start-timer #(swap! active-tasks remove-expired-tasks)
                     timer-interval))

(defn get-id []
  (apply str (repeatedly 8 #(rand-int 10))))

(defn response [body content]
  {:status 200
   :body body
   :headers {"Content-Type" content}})

(defn get-image [request]
  (let [{:keys [id type number]} (:params request)
        {:keys [question choices]} (@active-tasks id)]
    (-> (if (= type "question")
          question
          (nth choices (Integer/parseInt number)))
        (java.io.ByteArrayInputStream.)
        (response "image/png"))))

(defn tasks-info [_]
  (-> (manager/tasks-info)
      (json/json-str)
      (response "application/json")))

(defn generate-task [request]
  (let [{:keys [type level]} (:params request)
        task (manager/generate-task (keyword type)
                                    (dec (Integer/parseInt level)))
        id (get-id)]
    (->> (assoc task
           :timestamp (.getTime (Date.))
           :level level)
         (swap! active-tasks assoc id))
    (-> {:correct (:correct task)
         :id id}
        (json/json-str)
        (response "application/json"))))

(defn get-static-file [name]
  (let [th (Thread/currentThread)
        loader (.getContextClassLoader th)]
    (.getResourceAsStream loader (str "static/" name))))

(defn get-index-html [request]
  (response (get-static-file "index.html")
            "text/html"))

(defn add-result [request])

(def handler
  (app wrap-params wrap-keyword-params
       [""] get-index-html
       ["task"] generate-task
       ["image"] get-image
       ["tasks"] tasks-info))


(comment

  (do
    (require 'ring.adapter.jetty)
    (def server (ring.adapter.jetty/run-jetty
                 handler {:port 8080
                          :join? false
                          :daemon? true})))
  )
