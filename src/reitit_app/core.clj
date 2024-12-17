(ns reitit-app.core
  (:require [reitit.ring :as ring]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [muuntaja.core :as m]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as response]
            [reitit-app.feature-flags :refer [optional-routes flagged-routes feature-enabled?]]))

;; In-memory task storage
(defonce tasks (atom []))

;; Route handlers
(defn list-tasks [_]
  {:status 200
   :body @tasks
   :swagger {:responses {200 {:description "Successfully retrieved tasks"
                              :schema [:vector map?]}}}})

(defn create-task [req]
  (let [task (get-in req [:body-params :task])]
    (swap! tasks conj task)
    {:status 201
     :body task
     :swagger {:responses {201 {:description "Task created successfully"
                                :schema map?}}
               :parameters {:body-params {:task map?}}}}))

(defn delete-task [req]
  (let [task-to-delete (get-in req [:body-params :task])]
    (swap! tasks (fn [current-tasks]
                   (remove #(= % task-to-delete) current-tasks)))
    {:status 200
     :body "Task deleted"
     :swagger {:responses {200 {:description "Task deleted successfully"}}
               :parameters {:body-params {:task map?}}}}))

(defn update-task [req]
  (let [task-to-update (get-in req [:body-params :task])]
    {:status 200
     :body task-to-update
     :swagger {:responses {200 {:description "Task updated successfully"
                                :schema map?}}
               :parameters {:body-params {:task map?}}}}))

;; Swagger documentation routes
(def swagger-routes
  ["/swagger.json"
   {:get
    {:no-doc true
     :swagger {:info {:title "Task Manager API"
                      :description "Task Management API with Reitit"
                      :version "1.0.0"}
               :basePath "/"}
     :handler (swagger/create-swagger-handler)}}])

;; Base routes that are always enabled
(def base-routes
  [["/" {:get list-tasks
         :swagger {:tags ["tasks"]}}]
   ["/tasks" {:post create-task
              :swagger {:tags ["tasks"]}}]])

;; Optional delete routes
(def delete-routes
  (optional-routes
   #(feature-enabled? :enable-delete)
   [["/tasks" {:delete delete-task
               :swagger {:tags ["tasks"]}}]]))

;; Optional update routes
(def update-routes
  (optional-routes
   #(feature-enabled? :enable-update)
   [["/tasks" {:put update-task
               :swagger {:tags ["tasks"]}}]]))

;; Define the main application with routes and middleware
(def app
  (ring/ring-handler
   (ring/router
    (concat
     [swagger-routes
      ["/swagger/*" {:get (swagger-ui/create-swagger-ui-handler
                           {:url "/swagger.json"
                            :config {:validator-url nil}})}]]
     (flagged-routes
      base-routes
      delete-routes
      update-routes))
    {:data {:muuntaja m/instance
            :middleware [swagger/swagger-feature
                         parameters/parameters-middleware
                         muuntaja/format-middleware]}})))

;; Function to start the Jetty server
(defn start-server []
  (jetty/run-jetty #'app {:port 3000 :join? false}))

;; Main entry point of the application
(defn -main []
  (println "Starting server on port 3000...")
  (start-server))