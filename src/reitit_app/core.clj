(ns reitit-app.core
  (:require [reitit.ring :as ring]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.parameters :as parameters]
            [muuntaja.core :as m]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as response]))

;; In-memory task storage
(defonce tasks (atom []))

(defn list-tasks [_]
  {:status 200
   :body @tasks})

(defn create-task [req]
  (let [task (get-in req [:body-params :task])]
    (swap! tasks conj task)
    {:status 201
     :body task}))

(defn delete-task [req]
  (let [task-to-delete (get-in req [:body-params :task])]
    (swap! tasks (fn [current-tasks]
                   (remove #(= % task-to-delete) current-tasks)))
    {:status 200
     :body "Task deleted"}))

(def app
  (ring/ring-handler
   (ring/router
    [["/" {:get list-tasks}]
     ["/tasks" {:post create-task
                :delete delete-task}]]
    {:data {:muuntaja m/instance
            :middleware [parameters/parameters-middleware
                         muuntaja/format-middleware]}})))

(defn start-server []
  (jetty/run-jetty #'app {:port 3000 :join? false}))

(defn -main []
  (start-server))