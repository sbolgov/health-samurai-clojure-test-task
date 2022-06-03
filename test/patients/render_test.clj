(ns patients.render-test
  (:require [clojure.test :refer :all]
            [clojure.string :refer [blank?]]
            [patients.render :as render]))

(deftest render-select-checks
  (testing "render-select creates empty disabled option when value is empty"
    (let [[tag attrs & rest] (render/render-select "s1" "" "1" "One" "2" "Two")
          options (remove nil? rest)]
      (is (= :select tag))
      (is (= {:id "s1" :name "s1"} attrs))
      (is (= 3 (count options)))
      (is (= [:option {:value "" :disabled true :selected true} ""] (first options)))
      (is (= [:option {:value "1"} "One"] (second options)))
      (is (= [:option {:value "2"} "Two"] (last options)))))

  (testing "render-select does not create empty disabled option when value is not empty"
    (let [[tag attrs & rest] (render/render-select "s2" "2" "1" "One" "2" "Two")
          options (remove nil? rest)]
      (is (= :select tag))
      (is (= {:id "s2" :name "s2"} attrs))
      (is (= 2 (count options)))
      (is (= [:option {:value "1"} "One"] (first options)))
      (is (= [:option {:value "2" :selected true} "Two"] (second options))))))

(def patient1-data {
  :first_name "Fone"
  :middle_name "Mone"
  :last_name "Lone"
  :gender "M"
  :address1 "1 One st"
  :address2 "Apt. #1"
  :city "Cone"
  :state "Sone"
  :zip "11111"
  :country "USA"
  :policy "1111-1111-1111-1111"})

(defn is-text-input-for
  ([key] (is-text-input-for key true))
  ([key with-label]
    (let [input-name (name key)
          input-value (patient1-data key)
          rendered (render/render-input key patient1-data)
          div (first rendered)
          input (nth rendered (if with-label 3 1))
          [input-tag input-attrs] input
          rest (nthrest rendered (if with-label 4 2))]
      (is (= :div div))
      (when with-label
        (let [label (nth rendered 1)
              br (nth rendered 2)
              [label-tag label-attrs label-text] label]
          (is (= :label label-tag))
          (is (= {:for input-name} label-attrs))
          (is (not (blank? label-text)))
          (is (= [:br] br))))
      (is (= :input input-tag))
      (is (= {:type "text" :id input-name :name input-name :value input-value} input-attrs))
      (is (empty? rest)))))

(defn is-select-input-for [key]
  (let [input-name (name key)
        input-value (patient1-data key)
        [div label br input & rest] (render/render-input key patient1-data)
        [label-tag label-attrs label-text] label
        [input-tag input-attrs] input]
    (is (= :div div))
    (is (= :label label-tag))
    (is (= {:for input-name} label-attrs))
    (is (not (blank? label-text)))
    (is (= [:br] br))
    (is (= :select input-tag))
    (is (= {:id input-name :name input-name} input-attrs))
    (is (empty? rest))))

(deftest render-input-checks
  (testing "render-input creates"
    (testing "text input with label for first name"
      (is-text-input-for :first_name))
    (testing "text input with label for middle name"
      (is-text-input-for :middle_name))
    (testing "text input with label for last name"
      (is-text-input-for :last_name))
    (testing "select input with label for gender"
      (is-select-input-for :gender))
    (testing "text input with label for address line 1"
      (is-text-input-for :address1))
    (testing "text input with label for address line 2"
      (is-text-input-for :address2 false))
    (testing "text input with label for city"
      (is-text-input-for :city))
    (testing "text input with label for state"
      (is-text-input-for :state))
    (testing "text input with label for zip"
      (is-text-input-for :zip))
    (testing "text input with label for country"
      (is-text-input-for :country))
    (testing "text input with label for policy ID"
      (is-text-input-for :policy))))

(defn value-from-options [options]
  (let [[selected-option] (filterv #(:selected (second %)) options)]
    (if selected-option
      (:value (second selected-option))
      (throw (IllegalArgumentException. "No selected option found.")))))

(defn collect-values-from-inputs [acc elem]
  (if (string? elem)
    acc
    (let [tag (first elem)
          e2 (second elem)
          has-attrs (map? e2)
          attrs (if has-attrs e2 nil)
          rest (nthrest elem (if has-attrs 2 1))
          k (keyword (:name attrs))]
      (case tag
        :input
          (if (nil? k)
            (throw (IllegalArgumentException. (str "Missing :name attribute in  " elem)))
            (assoc acc k (:value attrs)))
        :select
          (if (nil? k)
            (throw (IllegalArgumentException. (str "Missing :name attribute in  " elem)))
            (assoc acc k (value-from-options rest)))
        (reduce collect-values-from-inputs acc rest)))))

(deftest render-update-form-checks
  (testing "render-update-form creates a form with data in proper inputs"
    (let [uuid (java.util.UUID/randomUUID)
          data (assoc patient1-data :patient_id uuid)
          [div attrs & rest] (render/render-update-form data)]
      (is (= :form div))
      (is (map? attrs))
      (is (= (str "/update/" uuid) (:action attrs)))
      (is (= patient1-data (reduce collect-values-from-inputs {} rest))))))

(deftest render-create-form-checks
  (testing "render-create-form creates a form with empty inputs"
    (let [[div attrs & rest] (render/render-create-form)
          empty-data (into {} (for [[k v] patient1-data] {k ""}))]
      (is (= :form div))
      (is (map? attrs))
      (is (= "/" (:action attrs)))
      (is (= empty-data (reduce collect-values-from-inputs {} rest))))))
