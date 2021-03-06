(ns harvester.db
    (:require [clojure.java.jdbc :as jdbc]
              [honeysql.core :as sql]
              [honeysql.helpers :as sql-fn]))

(defn- connstr
  [creds]
  (str "//"
       (:pg-host creds)
       ":"
       (:pg-port creds)
       "/"
       (:pg-database creds)))

(defn creds->db
  [creds]
  {:classname   "org.postgresql.Driver"
   :subprotocol "postgresql"
   :subname     (connstr creds)
   :user        (:pg-user creds)
   :password    (:pg-pass creds)})

(defn email-stored?
  [db email]
  (let [selector (-> (sql-fn/select :%count.*)
                     (sql-fn/from   :raw-mail)
                     (sql-fn/where [:= :mid (:mid email)])
                     sql/format)]
    (not
     (zero?
      (:count
       (first
        (jdbc/query db selector)))))))

(defn filter-stored
  [db messages]
  (filter #(not (email-stored? db %)) messages))

(defn store-message
  [conn msg]
  (jdbc/insert! conn
                :raw_mail
                {:mid          (:mid msg)
                 :sent         (:date-sent msg)
                 :received     (:date-received msg)
                 :addr_from    (:from msg)
                 :content_type (:content-type msg)
                 :multipart    (:multipart? msg)
                 :body         (:body msg)
                 :subject      (:subject msg)
                 :addr_to      (:to msg)}))


(defn store-messages
  [db messages]
  (jdbc/with-db-connection
    [conn db]
    (let [messages (filter-stored conn messages)]
      (doseq [msg messages]
        (store-message conn msg)))))

(defn db->message
  [msg]
  {:mid           (:mid msg)
   :date-sent     (:sent msg)
   :date-received (:received msg)
   :from          (:addr_from msg)
   :content-type  (:content_type msg)
   :multipart?    (:multipart msg)
   :body          (:body msg)
   :subject       (:subject msg)
   :to            (:addr_to msg)})

(defn db->messages
  [db]
 (let [selector (-> (sql-fn/select :*)
                     (sql-fn/from   :raw-mail)
                     sql/format)]
   (map db->message
        (jdbc/query db selector))))

