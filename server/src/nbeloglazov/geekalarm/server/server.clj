(ns nbeloglazov.geekalarm.server.server
  (:import [java.util Date])
  (:use [net.cgrand.moustache :only [app delegate]]
	[ring.middleware
	 [params :only (wrap-params)]
	 [keyword-params :only (wrap-keyword-params)]])
  (:require [nbeloglazov.geekalarm.server
	     [render-utils :as render]
	     [mathml-utils :as mathml]
	     [task-manager :as manager]
             [db :as db]
	     [utils :as utils]]
	    [clojure.contrib
	     [json :as json]]))

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

(def run-collector
     (memoize (fn [] (utils/start-timer #(swap! active-tasks remove-expired-tasks)
					timer-interval))))

(defn get-id []
  (apply str (repeatedly 3 #(rand-int 10))))

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
  (run-collector)
  (let [{:keys [category level]} (:params request)
	task (manager/get-task (keyword category)
			       (dec (Integer/parseInt level)))
	id (get-id)]
    (db/add-task {:category category
                  :level level
                  :time (Date.)})
    (->> (assoc task :timestamp (.getTime (Date.)))
	 (swap! active-tasks assoc id))
    (response (json/json-str {:id id
			      :correct (:correct task)})
	      "application/json")))

(defn get-static-file [name]
  (let [th (Thread/currentThread)
	loader (.getContextClassLoader th)]
    (.getResourceAsStream loader (str "static/" name))))

(defn get-index-html [request]
  (response (get-static-file "index.html")
	    "text/html"))

(def handler
     (app wrap-params wrap-keyword-params
      [""] get-index-html
      ["image"] get-image
      ["categories"] get-categories
      ["task"] get-task))