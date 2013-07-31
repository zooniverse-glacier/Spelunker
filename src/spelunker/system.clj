(ns spelunker.system
  (:require [spelunker.web.routes :as r]
            [spelunker.web.server :as s]
            [spelunker.model.mongo :as m]))

(defn system
  "Returns a new instance of the whole application"
  []
  {:mongo {:db "spelunker-devel"}
   :handler (r/routes)
   :port 8080})

(defn start
  [system]
  (let [server (s/create (:handler system)
                         :port (:port system))]
    (m/connect! (:mongo system))
    (into system {:server server})))

(defn stop
  [system]
  (when (:server system)
    (s/stop (:server system)))
  (m/disconnect!)
  (dissoc system :server))

(defn -main
  []
  (start (system)))
