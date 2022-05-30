(defproject patients "0.0.1-SNAPSHOT"
  :description "Patients CRUD"
  :license {:name "GNU General Public License"
            :url "http://www.gnu.org/licenses/gpl.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [com.github.seancorfield/next.jdbc "1.2.780"]
                 [org.postgresql/postgresql "42.3.6"]
                 [environ "1.2.0"]
                 ]
  :profiles {:dev {:plugins [[lein-environ "0.4.0"]
                            ]
                   :dependencies []
                   :source-paths ["dev"]
                   :env {:db-url "jdbc:postgresql://localhost/patients_db?user=healthsamuraitest"
                        }
                  }})
