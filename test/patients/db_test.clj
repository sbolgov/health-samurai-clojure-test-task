(ns patients.db-test
  (:require [clojure.test :refer :all]
            [environ.core :refer [env]]
            [next.jdbc :as jdbc]
            [clojure.java.io :as io]
            [patients.db :as db])
  (:import [com.opentable.db.postgres.embedded EmbeddedPostgres]
           [org.postgresql.util PSQLException]))

(def opts {:builder-fn next.jdbc.result-set/as-unqualified-lower-maps})

(defn select-all []
  (jdbc/execute! @db/ds ["select * from patients order by policy"] opts))

(-> (EmbeddedPostgres/builder)
    (.setPort (Integer/parseInt (env :pg-embedded-port)))
    (.start))

(jdbc/execute! @db/ds ["CREATE EXTENSION pgcrypto;"])
(jdbc/execute! @db/ds [(slurp (io/file "db.sql"))])

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

(def patient2-data {
  :first_name "Ftwo"
  :middle_name "Mtwo"
  :last_name "Ltwo"
  :gender "F"
  :address1 "2 Two st"
  :address2 "Apt. #2"
  :city "Ctwo"
  :state "Stwo"
  :zip "22222"
  :country "Canada"
  :policy "2222-2222-2222-2222"})

(def patient2-data-new {
  :first_name "Fnew"
  :middle_name "Mnew"
  :last_name "Lnew"
  :gender "M"
  :address1 "9 New st"
  :address2 "Apt. #9"
  :city "Cnew"
  :state "Snew"
  :zip "99999"
  :country "Mexico"
  :policy "2222-9999-9999-9999"})

(def patient3-data {
  :first_name "Abc"
  :middle_name "Def"
  :last_name "Ghi"
  :gender "M"
  :address1 "Jkl"
  :address2 "Nop"
  :city "Qrs"
  :state "Tvw"
  :zip "33333"
  :country "Xyz"
  :policy "4444-4444-4444-4444"})

; Create patient1
; Create another patient with the same policy number - fails
; Create patient2
; Read patient1 and patient2
; Update patient2
; Delete patient2
; Delete patient2 again
(deftest crud-checks
  (testing "Sanity check"
    (is (empty? (jdbc/execute! @db/ds ["select * from patients"]))))
  (let [id1 (db/create patient1-data)
        patient1 (assoc patient1-data :patient_id id1)]
    (testing "First patient created"
      (testing "with a valid id"
        (is (instance? java.util.UUID id1)))
      (testing "with all the data passed"
        (is (= [patient1] (select-all)))))

    (testing "Policy values are unique"
      (let [new-patient-with-same-policy (assoc patient2-data :policy (patient1-data :policy))]
        (is (thrown? PSQLException (db/create new-patient-with-same-policy)))))

    (let [id2 (db/create patient2-data)
          patient2 (assoc patient2-data :patient_id id2)]
      (testing "Second patient created"
        (testing "with all the data passed and existing records unchanged"
          (is (= [patient1 patient2] (select-all)))))

      (testing "db/get returns correct patients"
        (is (= patient1 (db/get id1)))
        (is (= patient2 (db/get id2))))

      (testing "Second patient updated"
        (let [patient2-new (assoc patient2-data-new :patient_id id2)
              res (db/update id2 patient2-data-new)]
          (testing "with all the data passed and existing records unchanged"
            (is (= #:next.jdbc{:update-count 1} res))
            (is (= [patient1 patient2-new] (select-all))))))

      (testing "Second patient deleted"
        (is (= id2 (db/delete id2)))
        (is (= [patient1] (select-all))))

      (testing "Repetitive deletions take no effect"
        (is (nil? (db/delete id2)))
        (is (= [patient1] (select-all)))))))

(deftest search-checks
  (testing "Sanity check"
    (is (empty? (jdbc/execute! @db/ds ["select * from patients"]))))

  (let [id1 (db/create patient1-data)
        id2 (db/create patient2-data)
        id3 (db/create patient3-data)
        patient1 (assoc patient1-data :patient_id id1)
        patient2 (assoc patient2-data :patient_id id2)
        patient3 (assoc patient3-data :patient_id id3)]
    (testing "Three patients created"
      (is (= [patient1 patient2 patient3] (select-all))))

    (testing "db/search"
      (testing "performs case-insensitive search"
        (testing "by first_name"
          (is (= [patient3] (db/search "ab" 9))))
        (testing "by middle_name"
          (is (= [patient3] (db/search "dE" 9))))
        (testing "by last_name"
          (is (= [patient3] (db/search "HI" 9))))
        (testing "by address1"
          (is (= [patient3] (db/search "K" 9))))
        (testing "by address2"
          (is (= [patient3] (db/search "no" 9))))
        (testing "by city"
          (is (= [patient3] (db/search "qr" 9))))
        (testing "by state"
          (is (= [patient3] (db/search "TV" 9))))
        (testing "by zip"
          (is (= [patient3] (db/search "3" 9))))
        (testing "by country"
          (is (= [patient3] (db/search "Y" 9))))
        (testing "by policy"
          (is (= [patient3] (db/search "4" 9)))))

      (testing "does not search by gender"
        ; patient1 and patient2 are here because their middle_names start with M.
        (is (= [patient1 patient2] (db/search "M" 9)))))

    (jdbc/execute! @db/ds ["delete from patients"])))

(deftest verifications-check
  (testing "db/verify"
    (is (= patient1-data (db/verify patient1-data))))

    (testing "fails on extra keys"
      (let [bad (assoc patient1-data :some 1)]
        (is (thrown? IllegalArgumentException (db/verify bad)))))

    (testing "fails on blank"
      (let [fails-on-blank (fn [k] (is (thrown? IllegalArgumentException
                                                (db/verify (assoc patient1-data k " ")))))]
        (testing "first_name"
          (fails-on-blank :first_name))
        (testing "last_name"
          (fails-on-blank :last_name))
        (testing "gender"
          (fails-on-blank :gender))
        (testing "address1"
          (fails-on-blank :address1))
        (testing "city"
          (fails-on-blank :city))
        (testing "state"
          (fails-on-blank :state))
        (testing "zip"
          (fails-on-blank :zip))
        (testing "country"
          (fails-on-blank :country))
        (testing "policy"
          (fails-on-blank :policy))))

    (testing "fails on invalid gender"
      (let [bad (assoc patient1-data :gender "U")]
        (is (thrown? IllegalArgumentException (db/verify bad))))))
