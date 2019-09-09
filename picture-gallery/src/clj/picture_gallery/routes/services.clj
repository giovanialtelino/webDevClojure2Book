(ns picture-gallery.routes.services
  (:require
    [reitit.swagger :as swagger]
    [reitit.swagger-ui :as swagger-ui]
    [reitit.ring.coercion :as coercion]
    [reitit.coercion.spec :as spec-coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.multipart :as multipart]
    [reitit.ring.middleware.parameters :as parameters]
    [reitit.dev.pretty :as pretty]
    [picture-gallery.middleware.formats :as formats]
    [picture-gallery.middleware.exception :as exception]
    [ring.util.http-response :refer :all]
    [clojure.java.io :as io]
    [schema.core :as sw]
    [picture-gallery.routes.services.auth :as auth]))

(sw/defschema UserRegistration
             {:id        String
              :pass              String
              :pass-confirm              String})
(sw/defschema Result
             {:result sw/Keyword
              (sw/optional-key :message) String})


(defn service-routes []
  ["/api"
   {:coercion spec-coercion/coercion
    :muuntaja formats/instance
    :swagger {:id ::api}
    :middleware [;; query-params & form-params
                 parameters/parameters-middleware
                 ;; content-negotiation
                 muuntaja/format-negotiate-middleware
                 ;; encoding response body
                 muuntaja/format-response-middleware
                 ;; exception handling
                 exception/exception-middleware
                 ;; decoding request body
                 muuntaja/format-request-middleware
                 ;; coercing response bodys
                 coercion/coerce-response-middleware
                 ;; coercing request parameters
                 coercion/coerce-request-middleware
                 ;; multipart
                 multipart/multipart-middleware]}

   ;; swagger documentation
   ["" {:no-doc true
        :swagger {:info {:title "my-api"
                         :description "https://cljdoc.org/d/metosin/reitit"}}}

    ["/swagger.json"
     {:get (swagger/create-swagger-handler)}]

    ["/api-docs/*"
     {:get (swagger-ui/create-swagger-ui-handler
             {:url "/api/swagger.json"
              :config {:validator-url nil}})}]]

   ["/math" {:post {:parameters {:body {:x int?, :y int?}}
                   :responses {200 {:body {:total pos-int?}}}
                   :handler (fn [{{{:keys [x y]} :body} :parameters}]
                              {:status 200
                               :body {:total (+ x y)}})}}]

   ["/register" {:post
                 {:parameters {:body {:user {:id string?
                                      :pass string?
                                      :pass-confirm string?}}}
                  :handler (fn [{{{:keys [user]} :body} :parameters}]
                             (auth/register! {{:keys :body}:parameters} user))}}]]
  )
