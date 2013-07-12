(ns spelunker.web.sky-server
  (:use spelunker.web.response)
  (:require [spelunker.model.sky-server :as s]))

(defn get-subjects
  [id]
  (try (if-let [query (s/get-query id)]
         (cond
          (= "ready" (:status query)) (resp-ok (assoc query :subjects (s/get-subjects query)))
          (= "error" (:status query)) resp-bad-params
          true (resp-ok query))
         resp-not-found)
       (catch Exception e resp-bad-params)))

(defn create-query
  [{:strs [params search_type]}]
  (let [query (s/query! params search_type)]
    (future (s/add-subjects! query (s/run-query query)))
    (resp-ok (s/get-query (:_id query)))))

