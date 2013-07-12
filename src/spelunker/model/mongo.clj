(ns spelunker.model.mongo
  (:use monger.operators
        [monger.conversion :only [to-object-id]])
  (:require [monger.core :as mg]
            [clojure.string :as str]
            [monger.collection :as mc]
            [net.cgrand.enlive-html :as en]
            [cemerick.url :as url]
            monger.json)
  (:import [org.bson.types ObjectId]))

(defn- subjects
  [collection]
  (str collection ":subjects"))

(defn- queries
  [collection]
  (str collection ":queries"))

(defn connect!
  [{:keys [db] :as opts}]
  (if-let [opts (dissoc opts :db)]
    (mg/connect! opts)
    (mg/connect!))
  (mg/set-db! (mg/get-db db)))

(def disconnect! mg/disconnect!)

(defn get-query
  "Retieve Query from Mongo"
  [collection id]
  (mc/find-map-by-id (str collection ":queries") (to-object-id id)))

(defn query!
  "Creates a new Query"
  [collection params search-type]
  (let [record {:params params
                :subject-uids []
                :search-type search-type
                :_id (ObjectId.)
                :status "not ready"}]
    (mc/insert (queries collection) record)
    (get-query collection (:_id record))))

(defn- query-url
  [url {:keys [params]}]
  (let [params params]
    (-> (url/url url)
        (assoc :query params)
        str
        java.net.URL.)))

(defn run-query
  "Runs a query and processes the results into a collection"
  [collection process-func url query]
  (try
    (let [doc (en/xml-resource (query-url url query))]
      (process-func doc))
    (catch Exception e (mc/update-by-id (queries collection) ))))

(defn add-subjects!
  "Adds Subjects to Database if they are not already there, updates them into
   query records"
  [collection query records]
  (mc/ensure-index (subjects collection) (array-map :uid 1) {:unique true})
  (let [uids (mc/distinct (subjects collection) "uid")
        records (filter #(not (some #{(:uid %)} uids)) records)]
    (mc/insert-batch (subjects collection) records))
  (mc/update-by-id (queries collection) (:_id query)
                   {$pushAll {:subject-uids (map #(:uid %) records)}
                    $set {:status "ready"}}))

(defn get-subjects
  "Retrives Subjects for Query"
  [collection query]
  (mc/find-maps (str collection ":subjects") {:uid {$in (:subject-uids query)}}))
