(ns reitit-app.core
  (:require [reitit.ring :as ring]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [muuntaja.core :as m]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as response]))         

;; In-memory task storage
(defonce tasks (atom []))                                   

;; Route handler for listing all tasks
(defn list-tasks [_]
  {:status 200
   :body @tasks
   :swagger {:responses {200 {:description "Successfully retrieved tasks"
                               :schema [:vector map?]}}}})

;; Route handler for creating a new task
(defn create-task [req]
  (let [task (get-in req [:body-params :task])]
    (swap! tasks conj task)
    {:status 201
     :body task
     :swagger {:responses {201 {:description "Task created successfully"
                                :schema map?}}
               :parameters {:body-params {:task map?}}}}))

;; Route handler for deleting a task
(defn delete-task [req]
  (let [task-to-delete (get-in req [:body-params :task])]
    (swap! tasks (fn [current-tasks]
                   (remove #(= % task-to-delete) current-tasks)))
    {:status 200
     :body "Task deleted"
     :swagger {:responses {200 {:description "Task deleted successfully"}}
               :parameters {:body-params {:task map?}}}}))                                

;; Swagger documentation routes
(def swagger-routes
  ["/swagger.json"
   {:get
    {:no-doc true
     :swagger {:info {:title "Task Manager API"
                      :description "Simple Task Management API - Reitit"
                      :version "1.0.0"}
               :basePath "/"}}
    :handler (swagger/create-swagger-handler)}])

;; Define the main application with routes and middleware
(def app
  (ring/ring-handler
   (ring/router
    [
     ;; Existing routes
     ["/" {:get list-tasks
           :swagger {:tags ["tasks"]}}]
     ["/tasks" {:post create-task
                :delete delete-task
                :swagger {:tags ["tasks"]}}]
     
     ;; Add Swagger routes
     swagger-routes
     
     ;; Add Swagger UI route
     ["/swagger/*" {:get (swagger-ui/create-swagger-ui-handler
                           {:url "/swagger.json"
                            :config {:validator-url nil}})}]
    ]
    {:data {:muuntaja m/instance
            :middleware [parameters/parameters-middleware
                         muuntaja/format-middleware
                         ;; Add Swagger middleware
                         swagger/swagger-feature]}})))  

;; Function to start the Jetty server
(defn start-server []
  (jetty/run-jetty #'app {:port 3000 :join? false}))        

;; Main entry point of the application
(defn -main []                                                
  (start-server))                                            
