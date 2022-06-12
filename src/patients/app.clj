(ns patients.app
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.params :as ring-params]
            [patients.handle :refer :all]
            [patients.render :refer :all]
))

(defroutes app-routes
  (GET "/" req handle-index)
  (GET "/edit/:id" [id] (handle-edit id))
  (POST "/" req handle-create)
  (POST "/update/:id" req handle-update)
  (POST "/delete/:id" req handle-delete)
  (GET "/search" req handle-search)
  (route/resources "/")
  (route/not-found "Not Found"))

(def app (ring-params/wrap-params app-routes))
