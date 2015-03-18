(ns harvester.mail
  (:require [clojure-mail.core :as mail]
            [clojure-mail.message :as message]
            [clj-time.core :as date]
            [clj-time.coerce]
            [clj-time.format :as date-format]))

(def date-formatter (date-format/formatter "E MMM d HH:mm:ss z yyyy"))

(defn load-store 
  [creds]
 (mail/gen-store (:mail-account creds)
                 (:mail-pass creds)))

(defn generate-mail-id
  [msg]
  (-> (:mid msg)
      (clojure.string/replace "<" "")
      (clojure.string/replace ">" "")))

(defn replace-mail-id
  [msg]
  (assoc msg
         :mid (generate-mail-id msg)))

(defn clean-message
  "Clean a message by replacing the date fields with Joda times,
  removing the (misspelt) date-recieved key, and producing a
  unique identifier for the message."
  [msg id]
  (replace-mail-id
   (dissoc
    (assoc msg
           :mid           id
           :from          (.toString (:from msg))
           :date-received (date-format/parse date-formatter
                                             (:date-recieved msg))
           :date-sent     (date-format/parse date-formatter
                                             (:date-sent msg)))
                       
    :date-recieved)))

(defn read-message
  [imap-message]
  (let [msg (message/read-message imap-message)]
    (clean-message msg (message/id imap-message))))

(defn- get-body-part-type
  [p]
  (clojure.string/lower-case
   (first
    (clojure.string/split (:content-type p) #";"))))

(defn get-alternative-body
  [body]
  (let [body (if (map? body) (vector body) body)
        preferred (filter
                   #(= (get-body-part-type %) "text/plain") body)]
    (if (empty? preferred)
      (:body (first body))
      (:body (first preferred)))))

(defn get-message
  [msg]
  (let [email (read-message msg)
        body  (get-alternative-body (:body email))]
    (assoc email
           :body
           body)))

(defn- get-type
  [m]
  (clojure.string/lower-case
   (first
    (clojure.string/split
     (message/content-type m) #";"))))

(defn parse-message
  [m]
  (let [message-type (get-type m)]
    (condp = message-type
      "text/plain" (get-message m)
      "multipart/alternative" (get-message m)
      "multipart/mixed" "h9"
      (str "don't know about that " message-type))))

(defn- can-parse [m]
  (let [ct (get-type m)]
    (or (= ct "text/plain")
        (= ct "text/alternative"))))

(defn jsonable [m]
  (assoc m
         :date-sent (clj-time.coerce/to-long (:date-sent m))
         :date-received (clj-time.coerce/to-long (:date-received m))))

(defn sqlable [m]
  (assoc m
         :date-sent (clj-time.coerce/to-sql-time (:date-sent m))
         :date-received (clj-time.coerce/to-sql-time (:date-received m))))

(defn retrieve-messages
  [creds n]
  (map (comp sqlable parse-message)
       (take n
             (filter can-parse
                     (mail/inbox (load-store creds))))))
