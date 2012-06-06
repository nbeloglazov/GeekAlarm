(defproject geekalarm-server "1.0.1"
  :description "Server for generating and serving tasks."
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/data.json "0.1.2"]
                 [org.clojure/math.numeric-tower "0.0.1"]
                 [org.clojure/math.combinatorics "0.0.2"]
		 [ring "1.1.0"]
		 [net.cgrand/moustache "1.0.0"]
		 [net.sourceforge.jeuclid/jeuclid-core "3.1.9"]
		 [incanter/incanter-core "1.3.0"]
                 [dk.brics.automaton/automaton "1.11.2"]
                 [org.clojure/core.match "0.2.0-alpha9"]]

  :ring {:handler com.geekalarm.server.server/handler
         :init com.geekalarm.server.server/run-collector}
  :target-dir "dotcloud"
  :uberjar-name "root.war")

