(ns harvester.core
  (:require [nomad :refer [defconfig]]
            [cheshire.core :as json]
            [clojure.java.io :as io]
            [clj-http.client :as http]
            [harvester.mail :as mail]
            [harvester.db :as db])
  (:gen-class))

(def +cred-file+ "harvester-creds.edn")
(defconfig +creds+ (slurp +cred-file+))

(defn elastic-upload
  [server message]
  (let [url (str server
                 "/mail/"
                 (:mail-account +creds+)
                 "/message/"
                 (:mid message))]
    (http/post url
               {:body         (json/generate-string message)
                :content-type :json
                :accept       :json})))

(def +mail-limit+ 20)
(defn run
  []
  (let [messages (mail/retrieve-messages (+creds+) +mail-limit+)]
    {:postgres
     (future
       (count
        (db/store-messages
         (db/creds->db (+creds+)) (map mail/sqlable messages))))
     :elasticsearch
     (future (pmap (partial elastic-upload (:elastic-host (+creds+)))
                   (map mail/sqlable messages)))}))

(defn -main
  "If run, do a collection."
  [& args]
  (map (fn [_ v]
         (deref v))
       (run)))



