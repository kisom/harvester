(defproject harvester "0.1.0-SNAPSHOT"
  :description "email->database"
  :license {:name "MIT license"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/java.jdbc "0.3.0"]
                 [org.postgresql/postgresql "9.2-1002-jdbc4"] 
                 [clojure-mail "0.1.6"]
		 [clj-time "0.9.0"]
                 [clj-http "1.0.1"]
                 [cheshire "5.4.0"]
                 [honeysql "0.5.1"]
                 [jarohen/nomad "0.7.0"]]
  :main ^:skip-aot harvester.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
