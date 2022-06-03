(ns patients.handle
  (:require [patients.db :as db]
            [patients.render :refer :all]
            [hiccup.core :as hiccup]
            [clojure.string :refer [blank?, lower-case]]
))

(defn parse-uuid [s]
  (if (blank? s)
      nil
      (try
        (java.util.UUID/fromString s)
      (catch IllegalArgumentException e
        nil))))

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
