(ns patients.app
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
))

(defroutes app
  (GET "/" [] "Hello Compojure World")
  (route/not-found "Not Found"))
