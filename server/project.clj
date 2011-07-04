(defproject geekalarm-server "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.2.1"]
		 [org.clojure/clojure-contrib "1.2.0"]
		 [ring "0.3.8"]
		 [net.cgrand/moustache "1.0.0"]
		 [net.sourceforge.jeuclid/jeuclid-core "3.1.9"]
		 [incanter/incanter-core "1.2.3"]]
  :dev-dependencies [[swank-clojure "1.2.0"]
		     [lein-ring "0.4.5"]
		     [ring-serve "0.1.0"]]
  :ring {:handler nbeloglazov.geekalarm.server.server/handler}
  :target-dir "dotcloud"
  :uberjar-name "root.war")

