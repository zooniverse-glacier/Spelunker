(ns spelunker.web.routes
  (:use ring.middleware.json
        ring.middleware.stacktrace)
  (:require [compojure.core :as cmpj :refer [GET POST context]]
            [compojure.route :refer [not-found]]
            [spelunker.web.sky-server :refer [get-subjects create-query]]))

(defn routes
  []
  (let [handler (cmpj/routes
                 (GET "/" [] "Hello World!")
                 (context "/sky_server" []
                          (GET "/" request "I like Jellies")
                          (GET "/:id" [id] (get-subjects id))
                          (POST "/" {params :params} (create-query params))))]
    (-> (wrap-json-response handler)
        wrap-json-params
        wrap-stacktrace)))
