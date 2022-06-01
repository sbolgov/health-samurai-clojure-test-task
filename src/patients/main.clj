(ns patients.main
  (:require [patients.config :as config])
  (:require [org.httpkit.server :refer [run-server]])
  (:require [patients.app :refer [app]])
  (:gen-class))

(defn -main [& args]
  (let [port (config/server-port)]
    (run-server app {:port port})
    (println (str "Started server on localhost:" port))))
