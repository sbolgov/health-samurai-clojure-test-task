(ns patients.main
  (:require [patients.config :as config])
  (:require [org.httpkit.server :refer [run-server]])
  (:gen-class))

(defn app [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "hello HTTP!"})

(defn -main [& args]
  (let [port (config/server-port)]
    (run-server app {:port port})
    (println (str "Started server on localhost:" port))))
