(ns html-templating.core
  (:require [selmer.parser :as selmer]
            [selmer.filters :as filters]))

(filters/add-filter! :empty? empty?)

(def run (selmer/render "Hello {{name}}" {:name "World"}))

(defn renderer []
  (wrap-error-page
    (fn [template]
      {:status 200
       :body (selmer/render-file template)})))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
