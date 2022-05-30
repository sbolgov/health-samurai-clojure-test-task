(ns patients.config
  (:require [environ.core :refer [env]]))

(defn db-url [] (env :db-url))
