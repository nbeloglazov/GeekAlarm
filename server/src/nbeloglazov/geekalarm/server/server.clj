(ns nbeloglazov.geekalarm.server.server
  (:use [net.cgrand.moustache :only [app delegate]]
	[ring.middleware
	 [params :only (wrap-params)]
	 [keyword-params :only (wrap-keyword-params)]])
  (:require [nbeloglazov.geekalarm.server
	     [render-utils :as render]
	     [mathml-utils :as mathml]
	     [task-manager :as manager]]
	    [clojure.contrib
	     [json :as json]]))

(def active-tasks (atom {}))

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
  (let [{:keys [category level]} (:params request)
	task (manager/get-task (keyword category) level)
	id (get-id)]
    (swap! active-tasks assoc id task)
    (response (json/json-str {:id id
			      :correct (:correct task)})
	      "application/json")))
  
(def handler
     (app wrap-params wrap-keyword-params
      ["image"] get-image
      ["categories"] get-categories
      ["task"] get-task))