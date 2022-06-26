(ns patients.render
  (:require [patients.db :as db]
            [clojure.string :refer [blank?, join]]
))

(defn join-non-blanks [sep & values]
  (join sep (remove blank? values)))

(defn render-patient-row [data]
  (let [link (str "/edit/" (:patient_id data))
        name (join-non-blanks " " (:first_name data) (:middle_name data) (:last_name data))
        address (join-non-blanks " "
                  (join-non-blanks ", " (:address1 data) (:address2 data) (:city data) (:state data))
                  (:zip data)
                  (:country data))
       ]
    [:tr
      [:td (:policy data)]
      [:td [:a {:href link} name]]
      [:td address]
    ]))

(defn render-patients-table [patients]
  (into [:table {:id "patients-table"}]
        (map render-patient-row patients)))

(defn render-index [patients]
  [:html
    [:body
      [:h1 {:class "title"} "Patients"]
      [:form {:action "/edit/new" :method "get" :id "create" :name "create" :style "display: inline;"}
        [:button {:type "submit" :style "display: inline;"} "Create new"]]
      [:div {:style "display: inline;"}
        [:span {:style "margin: 10px;"} "or Search"]
        [:input {:type "text" :id "search" :name "search" :autofocus true}]
      ]
      [:hr]
      [:div {:id "patients-list"} (render-patients-table patients)]
      [:script {:src "out/goog/base.js"}]
      [:script {:src "app.js"}]
      [:script "goog.require(\"patients.client\");"]
    ]])

(def labels {
  :first_name  "First name"
  :middle_name "Middle name"
  :last_name   "Last name"
  :gender      "Gender"
  :address1    "Address"
  :address2    ""
  :city        "City"
  :state       "State"
  :zip         "ZIP code"
  :country     "Country"
  :policy      "Policy number"
  })

(defn render-select [name value & options]
  (if (odd? (count options))
    (throw (IllegalArgumentException. (str "No value supplied for key: " (last options))))
    (into [:select {:id name :name name}]
      (cons
        (if (empty? value)
          [:option {:value "" :disabled true :selected true} ""]
          nil)
        (for [[v text] (partition 2 2 options)
              :let [attrs (if (= v value)
                              {:value v :selected true}
                              {:value v})]]
          [:option attrs text])))))

(defn render-text-input [name value]
  [:input {:type "text" :id name :name name :value value}])

(defn render-input [key data]
  (let [name (name key)
        label (key labels)
        value (key data "")]
    (into [:div] (concat
      (if (empty? label)
          nil
          [[:label {:for name} label] [:br]])
      [(cond
         (= :gender key)
           (render-select name value "F" "Female" "M" "Male")
         :else
           (render-text-input name value)
       )]))))

(defn render-data-form [action data]
  (into [:form {:id "edit" :action action :method "post"}]
    (list
      (render-input :first_name data)
      (render-input :middle_name data)
      (render-input :last_name data)
      (render-input :policy data)
      (render-input :gender data)
      (render-input :address1 data)
      (render-input :address2 data)
      (render-input :city data)
      (render-input :state data)
      (render-input :zip data)
      (render-input :country data)
    )))

(defn render-create-form []
  (render-data-form "/" {}))

(defn render-update-form [data]
  (render-data-form (str "/update/" (:patient_id data)) data))

(defn render-delete-form [id]
  [:form {:id "delete" :action (str "/delete/" id) :method "post"}])

(defn render-create-button []
  [:input {:type "submit" :form "edit"}])

(defn render-update-button [id]
  [:input {:type "submit" :form "edit" :value "Update"}])

(defn render-delete-button [id]
  [:input {:type "submit" :form "delete" :value "Delete"}])

(defn render-edit [id data]
  [:html
    (into [:body]
      (if (empty? id)
        [
          (render-create-form)
          (render-create-button)
        ]
        [
          (render-update-form data)
          (render-delete-form id)
          (render-update-button id)
          " "
          (render-delete-button id)
        ]))
    ])
