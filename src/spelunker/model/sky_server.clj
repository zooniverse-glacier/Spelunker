(ns spelunker.model.sky-server
  (:require [spelunker.model.mongo :as m]
            [clojure.string :as str]
            [net.cgrand.enlive-html :as e]))

(def collection "sky-server")

;; Functions to Generate Sky Server Compitable SQL

(defn- select-top
  [limit]
  (str "SELECT TOP " limit " g.objid, g.ra, g.dec, g.b, g.l, g.u, g.r, g.g, g.r, g.i, g.z, g.petroR90_u, g.petroR90_g, g.petroR90_r, g.petroR90_i, g.petroR90_z, s.plate, s.mjd, s.fiberID, s.specObjID, s.z as redshift"))

(def from-galaxy "FROM Galaxy as g")

(defn- with-coords
  [ra dec radius]
  (str ", dbo.fGetNearbyObjEq(" ra "," dec "," radius ") as n"))

(defn- join-to
  [prefix]
  (str "JOIN SpecObj as s ON s.bestobjid = " prefix ".objid"))

(def where-area "WHERE g.objid = n.objid")

(defn where-bands
  [params]
  (str "WHERE " (str/join " AND " (map (fn [[k v]] (str "g." k " " v)) params))))

(defmulti convert-params :search-type)

(defmethod convert-params "area" [{:keys [params]}]
  (let [{:keys [ra dec limit radius]} params]
    (str (select-top limit) " " from-galaxy (with-coords ra dec radius) " " (join-to "n") " " where-area)))

(defmethod convert-params "bands" [{:keys [params]}]
  (let [limit (:limit params)
        params (dissoc params :limit)]
    (str (select-top limit) " " from-galaxy " " (join-to "g") " " (where-bands params))))

(defmethod convert-params "sql" [{:keys [params]}]
  (:query params))

;; Implementation of Querying

(defn- row-to-map
  [row]
  (into {} (map (fn [tag] {(:tag tag) (first (:content tag))}) row)))

(defn- image-for-subject
  [subject]
  (let [{:keys [ra dec]} subject
        image-url (str "http://skyservice.pha.jhu.edu/DR9/ImgCutout/getjpeg.aspx?ra="
                       ra "&dec=" dec "&scale=0.15&width=300&height=300&opt=")]
    (assoc subject :image image-url)))

(defn- parse-sky-server
  "Convert SkyServer XML into clojure map."
  [xml]
  (->> (e/select xml [:Row])
       (map :content)
       (map row-to-map)
       (map (fn [subject]
              (let [uid (:objid subject)]
                (-> (dissoc subject :objid)
                    (assoc :uid uid)))))
       (map image-for-subject)))

(defn run-query
  [query] 
  (let [query (merge query {:params {:format "xml" :cmd (convert-params query)}})]
    (m/run-query collection parse-sky-server
                 "http://skyserver.sdss3.org/dr9/en/tools/search/x_sql.asp"
                 query)))

(def query! (partial m/query! collection))
(def get-query (partial m/get-query collection))
(def add-subjects! (partial m/add-subjects! collection))
(def get-subjects (partial m/get-subjects collection))
