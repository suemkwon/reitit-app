(ns reitit-app.feature-flags)

;; Store feature flags in an atom
(defonce feature-flags (atom {:enable-task-categories false
                             :enable-task-priority true
                             :enable-task-due-date false}))

(defn feature-enabled? [feature-key]
  (get @feature-flags feature-key false))

(defn toggle-feature! [feature-key]
  (swap! feature-flags update feature-key not)
  (get @feature-flags feature-key))

(defn list-features []
  @feature-flags)

;; Feature flag middleware
(defn wrap-feature-check [handler feature-key]
  (fn [request]
    (if (feature-enabled? feature-key)
      (handler request)
      {:status 403
       :body {:error (str "Feature " feature-key " is not enabled")}})))