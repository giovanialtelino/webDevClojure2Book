(ns swagger-service.core
  (:require [reagent.core :as reagent :refer [atom]]
            [ajax.core :refer [GET]]))

(defn images [links]
  [:div.text-xs-center
   (for [row (partition-all 3 links)]
     ^{:key row}
     [:div.row
      (for [link row]
        ^{:key link}
        [:div.col-sm-4 [:img {:width 400 :src link}]])])])

(defn fetch-links! [links link-count]
  (GET "/api/cat-links"
    {:params {:qtd link-count}
     :handler #(reset! links (vec (partition-all 6 %)))}))

(defn forwards [i pages]
  (if (< i (dec pages))(inc i) i))

(defn back [i]
  (if (pos? i) (dec i) i))

(defn nav-link [page i]
  [:li.page-item>a.page-link.btn.btn-primary
   {:on-click #(reset! page i)
    :class (when (= i @page) "Active")}
   [:span i]])

(defn pager [pages page]
  (when (> pages 1)
    (into
      [:div.text-cs-center>ul.pagination.pagination-lg]
      (concat
         [[:li.page-item>a.page-link.btn
           {:on-click #(swap! page back pages)
            :class (when (= @page 0) "disabled")}
           [:span "<<<"]]]
         (map (partial nav-link page) (range pages))
         [[:li.page-item>a.page-link.btn
           {:on-click #(swap! page forwards pages)
            :class (when (= @page (dec pages))"disabled")}
           [:span ">>>"]]]))))

(defn home-page []
  (let [links (atom nil)
        page (atom 0)]
    (fetch-links! links 50)
    (fn []
      (if (not-empty @links)
        [:div.container>div.row>div.col-md-12
         [pager (count @links) page]
         [images (@links @page)]]
        [:div "Standby by cats!"]))))

(defn mount-components []
  (reagent/render-component [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-components))
