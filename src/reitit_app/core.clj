(ns reitit-app.core
  (:require [reitit.ring :as ring]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [muuntaja.core :as m]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as response]
            [ring.middleware.resource :as resource]
            [ring.middleware.cors :refer [wrap-cors]]))

;; In-memory storage
(defonce tasks (atom []))
(defonce feature-flags (atom {:enable-task-categories false
                              :enable-task-priority true
                              :enable-task-due-date false}))

;; Feature flag functions
(defn feature-enabled? [feature-key]
  (get @feature-flags feature-key false))

(defn toggle-feature! [feature-key]
  (swap! feature-flags update feature-key not)
  (get @feature-flags feature-key))

(defn list-features []
  @feature-flags)

;; Task management functions
(defn list-tasks [_]
  {:status 200
   :body @tasks})

(defn create-task [req]
  (let [base-task (get-in req [:body-params :task])
        task (merge
              base-task
              (when (feature-enabled? :enable-task-priority)
                {:priority (get-in req [:body-params :priority] "medium")})
              (when (feature-enabled? :enable-task-categories)
                {:category (get-in req [:body-params :category] "general")})
              (when (feature-enabled? :enable-task-due-date)
                {:due-date (get-in req [:body-params :due-date])}))]
    (swap! tasks conj task)
    {:status 201
     :body task}))

(defn delete-task [req]
  (let [task-to-delete (get-in req [:body-params :task])]
    (swap! tasks (fn [current-tasks]
                   (remove #(= % task-to-delete) current-tasks)))
    {:status 200
     :body "Task deleted"}))

;; Feature flag endpoints
(defn list-feature-flags [_]
  {:status 200
   :body @feature-flags})

(defn toggle-feature [req]
  (let [feature-key (keyword (get-in req [:path-params :feature-key]))
        new-state (toggle-feature! feature-key)]
    {:status 200
     :body {:feature feature-key
            :enabled new-state}}))

;; Swagger documentation routes
(def swagger-routes
  ["/swagger.json"
   {:get
    {:no-doc true
     :swagger {:info {:title "Task Manager API"
                      :description "Task Management API with Feature Flags"
                      :version "1.0.0"}
               :basePath "/"}}
    :handler (swagger/create-swagger-handler)}])

;; Main application routes
(def app
  (-> (ring/ring-handler
       (ring/router
        [;; API routes
         ["/api"
          ["/" {:get list-tasks}]
          ["/tasks" {:post create-task
                     :delete delete-task}]
          ["/features"
           ["" {:get list-feature-flags}]
           ["/:feature-key" {:post toggle-feature}]]]

         ;; Swagger routes
         swagger-routes

         ["/swagger/*" {:get (swagger-ui/create-swagger-ui-handler
                              {:url "/swagger.json"
                               :config {:validator-url nil}})}]

         ;; UI route
         ["/" {:get (fn [_] (response/resource-response "index.html" {:root "public"}))}]]

        {:data {:muuntaja m/instance
                :middleware [parameters/parameters-middleware
                             muuntaja/format-middleware
                             swagger/swagger-feature]}})

       (ring/create-default-handler))

      ;; Add CORS middleware
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :post :put :delete]
                 :access-control-allow-headers ["Content-Type"])

      ;; Add resource middleware for serving static files
      (resource/wrap-resource "public")))

;; Server startup
(defn start-server []
  (jetty/run-jetty #'app {:port 3000 :join? false}))

(defn -main []
  (println "Starting server on port 3000...")
  (start-server))