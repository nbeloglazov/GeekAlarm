(defproject geekalarm-server "1.0.1"
  :description "Server for generating and serving tasks."
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/data.json "0.2.1"]
                 [org.clojure/math.numeric-tower "0.0.2"]
                 [org.clojure/math.combinatorics "0.0.3"]
                 [org.clojure/core.incubator "0.1.2"]
		 [ring "1.1.8"]
		 [net.cgrand/moustache "1.1.0"]
                 [org.clojure/core.match "0.2.0-alpha8"]
		 [incanter/incanter-core "1.4.0"]
                 [org.scilab.forge/jlatexmath "0.9.6"]
                 [dk.brics.automaton/automaton "1.11.2"]]
  :plugins [[lein-ring "0.8.2"]]
  :ring {:handler com.geekalarm.server.server/handler
         :init com.geekalarm.server.server/run-collector}
  :target-dir "dotcloud"
  :uberjar-name "root.war")

