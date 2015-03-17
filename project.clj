(defproject harvester "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.postgresql/postgresql "9.2-1002-jdbc4"] 
                 [clojure-mail "0.1.6"]
                 [honeysql "0.5.1"]
		 [xmpp-clj "0.3.1"]
		 [clojurewerkz/elastisch "2.2.0-beta2"]]
  :main ^:skip-aot harvester.core
  :plugins [[lein-cljsbuild "1.0.5"]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
