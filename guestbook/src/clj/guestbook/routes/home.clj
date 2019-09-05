(ns guestbook.routes.home
  (:require
    [guestbook.layout :as layout]
    [guestbook.db.core :as db]
    [guestbook.middleware :as middleware]
    [ring.util.response :refer [response status]]
    [guestbook.db.core :as db]
    [bouncer.core :as b]
    [bouncer.validators :as v]
    [guestbook.routes.ws :as ws]))

(defn validate-message [params]
  (first
    (b/validate
      params
      :name v/required
      :message [v/required [v/min-count 10]])))

(defn home-page [{:keys [flash]}]
  (layout/render flash "home.html"))

(defn about-page [request]
  (layout/render request "about.html"))

(defn save-message! [{:keys [params]}]
  (if-let [errors (validate-message params)]
    (-> {:errors errors} response (status 400))
    (do
      (db/save-message! (assoc params :timestamp (java.util.Date.)))
      (response {:status :ok}))))

(defn get-messages-hand [request]
  (response (vec (db/get-messages))))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/about" {:get about-page}]
   ["/message" {:post save-message!}]
   ["/getmessages" {:get get-messages-hand}]
   ["/ws" {:get ws/ws-handler}]])
