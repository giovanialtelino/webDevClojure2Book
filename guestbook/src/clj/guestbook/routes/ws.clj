(ns guestbook.routes.ws
  (:require [compojure.core :refer [GET POST defroutes]]
            [clojure.tools.logging :as log]
            [immutant.web.async :as async]
            [cognitect.transit :as transit]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [guestbook.db.core :as db]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.immutant :refer [sente-web-server-adapter]]))

(defonce channels (atom #{}))

(let [connection (sente/make-channel-socket!
                  sente-web-server-adapter
                   {:user-id-fn
                    (fn [ring-req] (get-in ring-req [:params :client-id]))})]
  (def ring-ajax-post (:ajax-post-fn connection))
  (def ring-ajax-get-or-ws-handshake (:ajax-get-or-ws-handshake-fn connection))
  (def ch-chsk (:ch-recv connection))
  (def chsk-send! (:send-fn connection))
  (def connected-uids (:connected-uids connection)))

;(defn connect! [channel]
;  (log/info "channel open")
;  (swap! channels conj channel))

;(defn disconnect!
;  [channel {:keys [code reason]}]
;  (log/info "close code: " code "reason: " reason)
;  (swap! channels #(remove #{channel} %)))

;(defn encode-transit [message]
;  (let [out (java.io.ByteArrayOutputStream. 4096)
;         writer (transit/writer out :json)
;    (transit/write writer message)
;    (.toString out)))

;(defn decode-transit [message]
;  (let [in (java.io.ByteArrayOutputStream. (.getBytes message))
;        reader (transit/reader in :json)
;    (transit/read reader)))

(defn validate-message [params]
  (first
    (b/validate
      params
      :name v/required
      :message [v/required [v/min-count 10]])))

(defn save-message! [message]
  (if-let [errors (validate-message message)]
    {:errors errors}
    (do
      (db/save-message! message)
      message)))

(defn handle-message! [{:keys [id client-id ?data]}]
  (println "\n\n+++++++++ GOT MESSAGE: " id (keys ?data))
  (when (= id :guestbook/add-message)
    (let [response (-> ?data
                     (assoc :timestamp (java.util.Date.))
                     save-message!)]
       (if (:errors response)
           (chsk-send! client-id (:guestbook/error response))
           (doseq [uid (:any @connected-uids)]
             (chsk-send! uid [:guestbook/add-message response]))))))

(defn stop-router! [stop-fn]
  (when stop-fn (stop-fn)))

(defn start-router! []
  (println "\n\n+++++++ Starting router +++++++\n\n")
  (sente/start-chsk-router! ch-chsk handle-message!))

(defstate router
  :start (start-router!)
  :stop (stop-router))

(defroutes websocket-routes
  (GET "/ws" req (ring-ajax-ajax-get-or-ws-handshake req))
  (POST "ws" req (ring-ajax-post req)))

(defn ws-handler [request]
  (async/as-channel
    request
    {:on-open connect!
     :on-close disconnect!
     :on-message handle-message!}))
