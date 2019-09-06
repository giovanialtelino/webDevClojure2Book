(ns db-examples.yesql
  (:require [db-examples.core :refer [db]]
            [clojure.java.jdbc :as sql]
            [com.layerware/hugsql :refer [defquery defqueries]]))

(defquery find-user "find-user.sql")
