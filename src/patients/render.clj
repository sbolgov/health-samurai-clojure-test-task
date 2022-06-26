(ns patients.render
  (:require [patients.db :as db]
            [clojure.string :refer [blank?]]
)
  (:import (java.util.regex Pattern)))

(defn highlight-matches [m s n]
  (lazy-seq
    (if (.find m)
      (let [start (.start m)
            end (.end m)
            non-highlighted (if (= start n) nil (subs s n start))
            highlighted (subs s start end)
            rest (highlight-matches m s end)
            r (cons [:span {:class "highlight" } highlighted] rest)]
          (if (nil? non-highlighted) r (cons non-highlighted r)))
      (let [t (subs s n)]
        (if (empty? t)
          nil
          (list t))))))

(defn text-with-highlights [re-pattern text]
  (cond
    (empty? text) nil
    (nil? re-pattern) (list text)
    :else (highlight-matches (.matcher re-pattern text) text 0)))

(defn join-texts-with-highlights [sep texts]
  (->> texts
       (remove empty?)
       (interpose (list sep))
       (apply concat)))

(defn render-patient-row [re-pattern data]
  (let [link (str "/edit/" (:patient_id data))
        render #(text-with-highlights re-pattern (data %))
        name (join-texts-with-highlights " " (map render [:first_name :middle_name :last_name]))
        address (join-texts-with-highlights " "
                  [(join-texts-with-highlights ", " (map render [:address1 :address2 :city :state]))
                   (render :zip)
                   (render :country)])
       ]
    [:tr
      (into [:td] (render :policy))
      [:td (into [:a {:href link}] name)]
      (into [:td] address)
    ]))

(defn build-highlight-re [highlight-str]
  (Pattern/compile highlight-str (+ Pattern/LITERAL Pattern/CASE_INSENSITIVE)))

(defn render-patients-table [patients highlight-str]
  (let [re-pattern (if (blank? highlight-str)
                       nil
                       (build-highlight-re highlight-str))]
    (into [:table {:id "patients-table"}]
          (map #(render-patient-row re-pattern %) patients))))

(defn render-index [patients]
  [:html
    [:head [:title "Patients"]
           [:style ".highlight { background-color:#FFFF00;}"]]
    [:body
      [:h1 {:class "title"} "Patients"]
      [:form {:action "/edit/new" :method "get" :id "create" :name "create" :style "display: inline;"}
        [:button {:type "submit" :style "display: inline;"} "Create new"]]
      [:div {:style "display: inline;"}
        [:span {:style "margin: 10px;"} "or Search"]
        [:input {:type "text" :id "search" :name "search" :autofocus true}]
      ]
      [:hr]
      [:div {:id "patients-list"} (render-patients-table patients nil)]
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
