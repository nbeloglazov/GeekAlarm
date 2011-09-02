(defproject geekalarm-server "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.2.1"]
		 [org.clojure/clojure-contrib "1.2.0"]
		 [ring "0.3.8"]
		 [net.cgrand/moustache "1.0.0"]
		 [net.sourceforge.jeuclid/jeuclid-core "3.1.9"]
		 [incanter/incanter-core "1.2.3"]
                 [dk.brics.automaton/automaton "1.11.2"]
                 [congomongo "0.1.6-SNAPSHOT"]]
  :dev-dependencies [[swank-clojure "1.2.0"]
		     [lein-ring "0.4.5"]
		     [ring-serve "0.1.0"]]
  :ring {:handler com.geekalarm.server.server/handler}
  :target-dir "dotcloud"
  :uberjar-name "root.war")

