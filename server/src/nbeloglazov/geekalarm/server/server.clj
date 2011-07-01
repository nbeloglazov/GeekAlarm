(ns nbeloglazov.geekalarm.server.server
  (:use [net.cgrand.moustache :only [app delegate]])
  (:require [nbeloglazov.geekalarm.server.euclid-utils :as render]
	    [nbeloglazov.geekalarm.server.mathml-utils :as mathml]
	    [nbeloglazov.geekalarm.server.generators.determinant :as determinant]))

(defn get-image [request]
  (let [task (determinant/generate 1)]
    {:status 200
     :body (euclid/cljml-to-stream (:question task))
     :content "image/png"}))
  
(def handler
     (app ["image"] get-image))