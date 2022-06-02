(ns patients.app
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.params :as ring-params]
            [patients.render :refer :all]
))

(defroutes app-routes
  (GET "/" [] render-index)
  (route/not-found "Not Found"))

(def app (ring-params/wrap-params app-routes))
