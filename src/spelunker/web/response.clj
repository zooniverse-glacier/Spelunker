(ns spelunker.web.response
  (:use [ring.util.response :only [content-type charset response status]]))

(defn- resp
  [body]
  (partial status (-> (response body)
                      (content-type "application/json")
                      (charset "utf-8"))))

(defn resp-ok
  [body]
  ((resp body) 200))

(def resp-no-content ((resp "") 204))

(def resp-accept ((resp "") 202))

(def resp-not-found ((resp {:status "Not Found"}) 404))

(def resp-bad-params ((resp {:status "Bad Parameters"}) 400))
