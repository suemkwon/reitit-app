(ns reitit-app.core
  (:require [reitit.ring :as ring]                         
            [reitit.ring.middleware.muuntaja :as muuntaja]   
            [reitit.ring.middleware.parameters :as parameters] 
            [muuntaja.core :as m]                          
            [ring.adapter.jetty :as jetty]                    
            [ring.util.response :as response]))               

;; In-memory task storage
(defonce tasks (atom []))                                   

;; Route handler for listing all tasks
(defn list-tasks [_]                                          
  {:status 200                                              
   :body @tasks})                                             ;

;; Route handler for creating a new task
(defn create-task [req]                                       
  (let [task (get-in req [:body-params :task])]                
    (swap! tasks conj task)                                 
    {:status 201                                                
     :body task}))                                            

;; Route handler for deleting a task
(defn delete-task [req]                                    
  (let [task-to-delete (get-in req [:body-params :task])]      
    (swap! tasks (fn [current-tasks]                           
                   (remove #(= % task-to-delete) current-tasks))) 
    {:status 200                                                
     :body "Task deleted"}))                                    

;; Define the main application with routes and middleware
(def app
  (ring/ring-handler
   (ring/router
    [["/" {:get list-tasks}]                                   
     ["/tasks" {:post create-task                               
                :delete delete-task}]                           
    ]
    {:data {:muuntaja m/instance                                
            :middleware [parameters/parameters-middleware      
                         muuntaja/format-middleware]}})))      

;; Function to start the Jetty server
(defn start-server []
  (jetty/run-jetty #'app {:port 3000 :join? false}))        

;; Main entry point of the application
(defn -main []                                                
  (start-server))                                            
