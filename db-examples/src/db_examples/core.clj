(ns db-examples.core
  (:require [clojure.java.jdbc :as sql]))
            
(def db {:subprotocol "postgresql"
         :subname "//localhost/REPORTING"
         :user "clojure"
         :password "testclojure"})

(defn create-users-table! []
  (sql/db-do-commands db
    (sql/create-table-ddl
      :users
      [[:id "varchar(32) PRIMARY KEY"]
       [:pass "varchar(100)"]])))

(defn get-user [id]
  (first (sql/query db ["select * from users where id = ?" id])))

(defn add-user! [user]
  (sql/insert! db :users user))

(defn add-users! [& users]
  (map add-user! users))

(defn set-pass! [id pass]
  (sql/update!
   db
   :users
   {:pass pass}
   ["id=?" id]))

(defn remove-user! [id]
  (sql/delete! db :users ["id=?" id]))

; still updating when not all true? why?!
; autocmmit is enabled by default on psql
(defn with-tran [x y]
  (sql/with-db-transaction [t-conn db]
    (sql/update! t-conn :users {:pass "xxxx3"} ["id=?" y])
    (sql/update! t-conn :users {:pass "yyyy3"} ["id=?" x])))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
