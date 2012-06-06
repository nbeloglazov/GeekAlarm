(ns com.geekalarm.server.server
  (:import [java.util Date])
  (:use [net.cgrand.moustache :only [app delegate]]
	[ring.middleware
	 [params :only (wrap-params)]
	 [keyword-params :only (wrap-keyword-params)]])
  (:require [com.geekalarm.server
             [render-utils :as render]
	     [mathml-utils :as mathml]
	     [task-manager :as manager]
	     [utils :as utils]]
	    [clojure.data.json :as json]))


(def task-timeout (* 10 60 1000))

(def timer-interval (* 1 1000))

(def active-tasks (atom {}))

(defn remove-expired-tasks [tasks]
  (let [lower-bound (- (.getTime (Date.))
		       task-timeout)]
    (->> tasks
	 (filter (fn [[id task]]
		   (>= (:timestamp task) lower-bound)))
	 (into {}))))

(defn run-collector []
  (utils/start-timer #(do (println "!!!!") (swap! active-tasks remove-expired-tasks))
                     timer-interval))

(defn get-id []
  (apply str (repeatedly 8 #(rand-int 10))))

(defn response [body content]
  {:status 200
   :body body
   :content content})

(defn get-image [request]
  (let [{:keys [id type number]} (:params request)
	{:keys [question choices]} (@active-tasks id)]
    (-> (if (= type "question")
	  question
	  (nth choices (dec (bigint number))))
	(response "image/png"))))

(defn get-categories [request]
  (-> (manager/get-categories)
      (json/json-str)
      (response "application/json")))

(defn get-task [request]
  (let [{:keys [category level]} (:params request)
	task (manager/get-task (keyword category)
			       (dec (Integer/parseInt level)))
	id (get-id)]
    (->> (assoc task
           :timestamp (.getTime (Date.))
           :category category
           :level level)
	 (swap! active-tasks assoc id))
    (-> (select-keys task [:info :name :correct])
        (assoc :id id)
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
      ["image"] get-image
      ["categories"] get-categories
      ["task"] get-task
      ["result"] add-result))