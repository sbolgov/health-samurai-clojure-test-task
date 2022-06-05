(ns patients.handle
  (:require [patients.db :as db]
            [patients.render :refer :all]
            [hiccup.core :as hiccup]
            [clojure.string :refer [blank?, join, lower-case, split]]
))

(defn parse-uuid [s]
  (if (blank? s)
      nil
      (try
        (java.util.UUID/fromString s)
      (catch IllegalArgumentException e
        nil))))

(defn redirect-to-home [ & tops]
  (let [url (if (empty? tops)
                "/"
                (str "/?top=" (join "," tops)))]
  {:status 303 :headers {"Location" url}}))

(defn handle-index [req]
  (let [param-top (get (:params req) "top" "")
        top-list (->> (split param-top #",")
                      (map parse-uuid)
                      (remove nil?)
                      (map db/get)
                      (remove nil?))
        top-uuids (into #{} (map :patient_id top-list))
        other-list (->> (db/search "" 10)
                        (remove #(contains? top-uuids (:patient_id %))))
       ]
    (hiccup/html (render-index (concat top-list other-list)))))

(defn handle-edit [id]
  (if (= (lower-case id) "new")
      (hiccup/html (render-edit nil {}))
      (let [uuid (parse-uuid id)]
        (if (nil? uuid)
            {:status 400}
            (let [data (db/get uuid)]
              (if (empty? data)
                  {:status 404}
                  (hiccup/html (render-edit id data))))))))

(defn handle-create [req]
  (let [params (:params req)
        data (into {} (map (fn [k] [k (get params (name k))]) db/valid-keys))]
  (try
    (let [uuid (db/create data)]
      (redirect-to-home uuid))
  (catch IllegalArgumentException e
    {:status 400 :body (.getMessage e)}))))

(defn handle-update [req]
  (let [param-id (get (:route-params req) :id)
        uuid (parse-uuid param-id)]
    (if (nil? uuid)
        {:status 400}
        (let [data (into {} (map (fn [[k v]] {(keyword k) v}) (:form-params req)))
              updated-uuid (db/update uuid data)]
          (if (nil? updated-uuid)
              {:status 404}
              (redirect-to-home uuid))))))

(defn handle-delete [req]
  (let [param-id (get (:route-params req) :id)
        uuid (parse-uuid param-id)]
    (if (nil? uuid)
        {:status 400}
        (let [deleted-uuid (db/delete uuid)]
          (if (nil? deleted-uuid)
              {:status 404}
              (redirect-to-home))))))
