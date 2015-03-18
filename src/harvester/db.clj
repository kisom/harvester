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
  (not
   (zero?
    (:count
     (first
      (jdbc/query db
                  (-> (sql-fn/select :%count.*)
                      (sql-fn/from   :raw-mail)
                      (sql-fn/where [:= :mid (:mid email)])
                      sql/format)))))))

(defn filter-stored
  [db messages]
  (filter #(not (email-stored? db %))
          messages))

(defn store-message
  [conn msg]
  (let [from (keyword "\"from\"")
        to   (keyword "\"to\"")]
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
                   :addr_to      (:to msg)})))

(defn store-messages
  [db messages]
  (let [messages (filter-stored db messages)]
    (jdbc/with-db-connection
      [conn db]
      (doseq [msg messages]
        (store-message conn msg)))))

