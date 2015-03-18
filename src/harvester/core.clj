(ns harvester.core
  (:require [nomad :refer [defconfig]]
            [clojure.java.io :as io]
            [harvester.mail :as mail]
            [harvester.db :as db])
  (:gen-class))

(def +cred-file+ "harvester-creds.edn")
(defconfig +creds+ (slurp +cred-file+))

(def +mail-limit+ 100)
(defn run
  []
  (count
   (db/store-messages
    (db/creds->db (+creds+))
    (mail/retrieve-messages (+creds+) +mail-limit+))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))



