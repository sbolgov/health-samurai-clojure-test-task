(ns patients.client
  (:require [ajax.core :refer [GET]]))

(defn ^:export search-patients [e]
  (let [search-str (-> e .-target .-value)
        handler (fn [response]
                  (-> js/document
                      (.getElementById "patients-list")
                      (.-innerHTML)
                      (set! response)))
       ]
    (GET "/search" {:params {:q search-str} :handler handler})))

(defn ^:export window-loaded []
  (-> js/document
      (.getElementById "search")
      (.addEventListener "input" search-patients false)))

(.addEventListener js/window "load" window-loaded false)
