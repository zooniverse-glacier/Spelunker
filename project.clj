(defproject spelunker "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.cemerick/url "0.0.8"]
                 [com.novemberain/monger "1.6.0-beta3"]
                 [org.clojure/algo.generic "0.1.1"]
                 [compojure "1.1.5"]
                 [ring/ring-json "0.2.0"]
                 [enlive "1.1.1"]
                 [ring/ring-devel "1.2.0-RC1"]
                 [ring/ring-jetty-adapter "1.2.0-RC1"]]
  :profiles
  {:dev {:source-paths ["dev"]
         :dependencies [[ring-mock "0.1.5"]
                        [org.clojure/tools.namespace "0.2.3"]]}})
