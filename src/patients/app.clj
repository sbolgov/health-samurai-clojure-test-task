(ns patients.app
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.params :as ring-params]
            [patients.handle :refer :all]
            [patients.render :refer :all]
))

(defroutes app-routes
  (GET "/" [] render-index)
  (GET "/edit/:id" [id] (handle-edit id))
  (route/not-found "Not Found"))

(def app (ring-params/wrap-params app-routes))
