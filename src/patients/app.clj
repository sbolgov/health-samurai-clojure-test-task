(ns patients.app
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.params :as ring-params]
))

(defroutes app-routes
  (GET "/" [] "Hello Compojure World")
  (route/not-found "Not Found"))

(def app (ring-params/wrap-params app-routes))
