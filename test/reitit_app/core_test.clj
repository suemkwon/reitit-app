(ns reitit-app.core-test
  (:require [clojure.test :refer :all]
            [reitit-app.core :refer :all]
            [ring.mock.request :as mock]))

;; Test adding a task
(deftest test-add-task
  (let [response (app (mock/request :post "/tasks/new" {:params {:name "Test Task"}}))]
    (is (= 201 (:status response)))
    (is (:task-id (:body response)))))

;; Test listing tasks
(deftest test-list-tasks
  (add-task "Test Task 1")
  (add-task "Test Task 2")
  (let [response (app (mock/request :get "/tasks"))]
    (is (= 200 (:status response)))
    (is (>= (count (:body response)) 2))))

;; Test deleting a task
(deftest test-delete-task
  (let [task-id (add-task "Test Task to Delete")
        response (app (mock/request :delete (str "/tasks/" task-id)))]
    (is (= 204 (:status response)))
    (is (not (get @tasks task-id)))))

;; Run the tests
(run-tests)
