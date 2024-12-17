(ns reitit-app.feature-flags-test
  (:require [clojure.test :refer :all]
            [reitit-app.core :refer [app]]
            [reitit-app.feature-flags :refer [feature-flags]]
            [ring.mock.request :as mock]
            [muuntaja.core :as m]))

(defn- json-body [response]
  (m/decode "application/json" (:body response)))

(deftest test-feature-flags
  (testing "Base routes are always available"
    (let [response (app (-> (mock/request :get "/")))]
      (is (= 200 (:status response)))))

  (testing "Delete endpoint is available when enabled"
    (reset! (get feature-flags :enable-delete) true)
    (let [response (app (-> (mock/request :delete "/tasks")
                            (mock/json-body {:task {:id 1}})))]
      (is (= 200 (:status response))))

    (reset! (get feature-flags :enable-delete) false)
    (let [response (app (-> (mock/request :delete "/tasks")
                            (mock/json-body {:task {:id 1}})))]
      (is (= 404 (:status response)))))

  (testing "Update endpoint is available when enabled"
    (reset! (get feature-flags :enable-update) true)
    (let [response (app (-> (mock/request :put "/tasks")
                            (mock/json-body {:task {:id 1 :name "Updated"}})))]
      (is (= 200 (:status response))))

    (reset! (get feature-flags :enable-update) false)
    (let [response (app (-> (mock/request :put "/tasks")
                            (mock/json-body {:task {:id 1 :name "Updated"}})))]
      (is (= 404 (:status response))))))

(deftest test-swagger-documentation
  (testing "Swagger documentation reflects feature flags"
    (reset! (get feature-flags :enable-delete) true)
    (reset! (get feature-flags :enable-update) false)
    (let [response (app (mock/request :get "/swagger.json"))
          swagger-spec (json-body response)
          paths (get swagger-spec "paths")]
      (is (= 200 (:status response)))
      (is (get-in paths ["/tasks" "delete"]) "Delete endpoint should be in docs when enabled")
      (is (nil? (get-in paths ["/tasks" "put"])) "Update endpoint should not be in docs when disabled"))))