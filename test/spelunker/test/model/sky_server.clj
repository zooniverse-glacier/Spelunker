(ns spelunker.test.model.sky-server
  (:use clojure.test
        spelunker.model.sky-server))

(deftest sky-server-area
  (let [query {:search-type "area"
               :params {:ra 180 :dec 10 :radius 50 :limit 5}
               :subject-uids []}]
    (run-query query)))
