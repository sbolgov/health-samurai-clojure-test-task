(ns patients.render
  (:require [hiccup.core :as hiccup])
  )

(defn render-index [req]
  (hiccup/html
    [:html
      [:body
        [:h1 {:class "title"} "Patients"]
        [:form {:action "/edit/new" :method "get" :id "create" :name "create" :style "display: inline;"}
          [:button {:type "submit" :style "display: inline;"} "Create new"]]
        [:div {:style "display: inline;"}
          [:span {:style "margin: 10px;"} "or Search"]
          [:input {:type "text" :id "search" :name "search" :autofocus true}]
        ]
        [:div {:id "patients-list"}]
      ]]))
