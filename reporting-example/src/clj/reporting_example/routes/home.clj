(ns reporting-example.routes.home
  (:require
    [reporting-example.layout :as layout]
    [reporting-example.db.core :as db]
    [clojure.java.io :as io]
    [reporting-example.middleware :as middleware]
    [ring.util.response :as response]
    [reporting-example.routes.reports :as reports]))

(defn write-response [report-bytes]
  (with-open [in (java.io.ByteArrayInputStream. report-bytes)]
    (-> (response/response in)
        (response/header "Content-Disposition" "filename=document.pdf")
        (response/header "Content-Length" (count report-bytes))
        (response/content-type "application/pdf"))))

(defn generate-report [route]
  (try
    (let [out (java.io.ByteArrayOutputStream.)]
      (cond
       (= route :list) (reports/list-report out)
       (= route :table) (reports/table-report out))
      (write-response (.toByteArray out)))
    (catch Exception ex
      (layout/render nil "home.html" {:error (.getMessage ex)}))))

(defn generate-list-report [request]
  (generate-report :list))

(defn generate-table-report [request]
  (generate-report :table))

(defn home-page [request]
  (layout/render request "home.html"))

(defn about-page [request]
  (layout/render request "about.html"))

(defn home-routes []
  [""
   {:middleware [
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/about" {:get about-page}]
   ["/table" {:get generate-table-report}]
   ["/list" {:get generate-list-report}]])
