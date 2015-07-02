(defproject geekalarm-server "1.0.1"
  :description "Server for generating and serving tasks."
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [org.clojure/math.combinatorics "0.1.1"]
                 [org.clojure/core.incubator "0.1.3"]
                 [ring "1.3.2"]
                 [net.cgrand/moustache "1.1.0"]
                 [org.clojure/core.match "0.2.2"]
                 [incanter/incanter-core "1.5.6"]
                 [org.scilab.forge/jlatexmath "1.0.2"]
                 [dk.brics.automaton/automaton "1.11.2"]]
  :plugins [[lein-ring "0.8.10"]
            [lein-immutant "1.2.0"]]
  :profiles {:dev {:dependencies [[quil "2.2.6"]]}}
  :ring {:handler com.geekalarm.server.server/handler
         :init com.geekalarm.server.server/run-collector}
  :uberjar-name "root.war")

