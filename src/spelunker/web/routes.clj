(ns spelunker.web.routes
  (:use ring.middleware.json
        ring.middleware.stacktrace
        [ring.middleware.cors :only [wrap-cors]])
  (:require [compojure.core :as cmpj :refer [OPTIONS GET POST context]]
            [compojure.route :refer [not-found]]
            [spelunker.web.response :refer [resp-ok]]
            [spelunker.web.sky-server :refer [get-subjects create-query]]))

(defn routes
  []
  (let [handler (cmpj/routes
                 (OPTIONS "/*" [] (resp-ok ""))
                 (GET "/" [] "Hello World!")
                 (context "/sky_server" []
                          (GET "/:id" [id] (get-subjects id))
                          (POST "/" {params :params} (create-query params))))]

    (-> (wrap-json-response handler)
        wrap-json-params
        wrap-stacktrace)))
