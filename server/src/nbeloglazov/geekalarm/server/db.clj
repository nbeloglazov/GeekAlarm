(ns nbeloglazov.geekalarm.server.db
  (:require [somnium.congomongo :as mongo]
            [clojure.contrib.json :as json]))

(def *username* "geek")
(def *password* "alarm")
(def *db* "geekalarm")

(defn get-mongodb-environment [env]
  (let [params (->> (slurp env)
                    (json/read-json))]
    {:host (params :DOTCLOUD_DB_MONGODB_HOST)
     :port (Integer/parseInt
            (params :DOTCLOUD_DB_MONGODB_PORT))}))
    

(defn get-environment []
  (let [env (java.io.File. "/home/dotcloud/environment.json")]
    (if (.exists env)
      (get-mongodb-environment env)
      {})))

(def get-connection
     (memoize
      (fn []
        (let [con (mongo/make-connection *db* (get-environment))]
          (mongo/authenticate con *username* *password*)
          con))))

(defn add-task [task]
  (mongo/with-mongo (get-connection)
    (mongo/insert! "requests" task))) 



        