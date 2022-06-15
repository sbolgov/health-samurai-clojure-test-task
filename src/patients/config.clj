(ns patients.config
  (:require [environ.core :refer [env]]))

(defn db-url [] (env :db-url))

(defn server-port [] (Integer/parseInt (env :port)))
