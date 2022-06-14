(ns patients.db
  (:require [patients.config :as config])
  (:require [clojure.set :as set])
  (:require [clojure.string :refer [blank?, join]])
  (:require [next.jdbc :as jdbc])
  (:require [next.jdbc.result-set :as rs]))

(def ds (delay (jdbc/get-datasource (config/db-url))))

(defn get [id]
  (let [opts {:builder-fn rs/as-unqualified-lower-maps}
        sql-params ["select * from patients where patient_id = ?" id]]
    (jdbc/execute-one! @ds sql-params opts)))

(defn delete [id]
  (let [opts {:return-keys true :builder-fn rs/as-unqualified-lower-maps}
        sql-params ["delete from patients where patient_id = ?" id]]
    (-> (jdbc/execute-one! @ds sql-params opts)
        (:patient_id))))

(def valid-keys #{
  :first_name
  :middle_name
  :last_name
  :gender
  :address1
  :address2
  :city
  :state
  :zip
  :country
  :policy
  })

(def searchable-fields (map name (disj valid-keys :gender)))

(defn verify [data]
  (do
    ; Check keys.
    (let [ks (keys data)
          diff (set/difference (set ks) valid-keys)]
      (if (empty? diff)
         true
         (throw (IllegalArgumentException. (str "Unexpected keys: " (join "," diff))))))
    ; Check mandatory values.
    (doall (for [k [:first_name :last_name :gender :address1 :city :state :zip :country :policy]
                 :let [v (data k)]
                 :when (blank? v)]
             (throw (IllegalArgumentException. (str "Field " (name k) " cannot be blank")))))
    ; Check gender.
    (case (:gender data)
      ("F" "M") true
      (throw (IllegalArgumentException. (str "Unexpected gender: " (:gender data)))))
    ; Everything looks good.
    data))

(defn create [data]
  (let [opts {:return-keys true :builder-fn rs/as-unqualified-lower-maps}
        fields (map name (keys (verify data)))
        n (count fields)
        sql (str "insert into patients ("
                   (join ", " fields)
                   ") values ("
                   (join "," (repeat n "?"))
                   ")")
        sql-params (cons sql (vals data))
       ]
    (-> (jdbc/execute-one! @ds sql-params opts)
        (:patient_id))))

(defn update [id data]
  (let [fields (map name (keys (verify data)))
        sql (str "update patients set "
                   (join ", " (map #(str % "=?") fields))
                   " where patient_id=?")
        vs (concat (vals data) [id])
        sql-params (cons sql vs)
       ]
    (jdbc/execute-one! @ds sql-params)))

(defn search [part max-rows]
  (let [opts {:max-rows max-rows :builder-fn rs/as-unqualified-lower-maps}
        sql-params (if (empty? part)
                      ["select * from patients order by last_name, first_name, middle_name"]
                      (let [n (count searchable-fields)
                            where-cond (->> searchable-fields
                                            (map #(str "lower(" % ") like lower('%' || ? || '%')"))
                                            (join " or "))
                            sql (str "select * from patients where " where-cond " order by last_name, first_name, middle_name")
                           ]
                        (cons sql (repeat n part))))
       ]
    (jdbc/execute! @ds sql-params opts)))
