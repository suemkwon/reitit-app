(defproject reitit-app "0.1.0-SNAPSHOT"
  :description "Simple Task Manager"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [metosin/reitit "0.7.0-alpha7"]
                 [ring/ring-jetty-adapter "1.9.6"]
                 [metosin/muuntaja "0.6.8"]]
  :main reitit-app.core)
