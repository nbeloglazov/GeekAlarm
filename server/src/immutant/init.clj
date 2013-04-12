(ns immutant.init
  (:require [com.geekalarm.server.server :as server])
  (:require [immutant.messaging :as messaging]
            [immutant.web :as web]
            [immutant.util :as util]))

;; This file will be loaded when the application is deployed to Immutant, and
;; can be used to start services your app needs. Examples:


;; Web endpoints need a context-path and ring handler function. The context
;; path given here is a sub-path to the global context-path for the app
;; if any.

(web/start "/" server/handler)

(server/run-collector)
; (web/start "/foo" a-different-ring-handler)

;; To start a Noir app:
; (server/load-views (util/app-relative "src//views"))
; (web/start "/" (server/gen-handler {:mode :dev :ns 'kibit}))


;; Messaging allows for starting (and stopping) destinations (queues & topics)
;; and listening for messages on a destination.

; (messaging/start "/queue/a-queue")
; (messaging/listen "/queue/a-queue" #(println "received: " %))

