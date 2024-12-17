(ns reitit-app.feature-flags)

(defn optional-routes
  "Takes a predicate function and routes. Returns the routes if predicate returns true, nil otherwise."
  [pred routes]
  (when (pred)
    routes))

(defn flagged-routes
  "Combines multiple route definitions, filtering out nil values."
  [& routes]
  (vec (remove nil? (flatten routes))))

;; Example feature flag predicates
(def feature-flags
  {:enable-delete (atom true)
   :enable-update (atom false)})

(defn feature-enabled? [flag-key]
  (if-let [flag-atom (get feature-flags flag-key)]
    @flag-atom
    false))