(ns patients.handle
  (:require [patients.db :as db]
            [patients.render :refer :all]
            [hiccup.core :as hiccup]
            [clojure.string :refer [blank?, lower-case, split]]
))

(defn parse-uuid [s]
  (if (blank? s)
      nil
      (try
        (java.util.UUID/fromString s)
      (catch IllegalArgumentException e
        nil))))

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
