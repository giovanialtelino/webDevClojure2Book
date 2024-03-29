(ns picture-gallery.routes.services.auth
  (:require [picture-gallery.db.core :as db]
            [ring.util.http-response :as response]
            [buddy.hashers :as hashers]
            [clojure.tools.logging :as log]
            [picture-gallery.validation :refer [registration-errors]]))

(defn handle-registration-error [e]
  (if (-> e (.getNextException)
            (.getMessage)
            (.startsWith "Error: duplicate key value"))
      (response/precondition-failed {:result :error
                                     :message "user with selected ID already exists"})
      (do
        (log/error e)
        (response/internal-server-error {
                                         :result :error
                                         :message "server error occurred while adding the user"
                                         }))))

(defn register! [{:keys [session]} user]
  (println user)
  (if (registration-errors user)
    (response/precondition-failed {:result :error})
  (try
    (db/create-user!
      ( -> user
          (dissoc :pass-confirm)
          (update :pass hashers/encrypt)))
      ( -> {:result :ok}
           (response/ok)
           (assoc :session (assoc session :identity (:id user))))
    (catch Exception e
        (log/error e)
        (response/internal-server-error
          {:result :error
           :message "server error while adding the user"})
      ))))
