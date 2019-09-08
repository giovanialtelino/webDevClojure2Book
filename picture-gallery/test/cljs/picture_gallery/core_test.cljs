(ns picture-gallery.core-test
  (:require [cljs.test :refer-macros [is are deftest testing use-fixtures]]
            [pjstadig.humane-test-output]
            [picture-gallery.core :as rc]))

(deftest test-home
  (is (= true true)))

