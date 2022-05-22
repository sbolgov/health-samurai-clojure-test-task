(defproject patients "0.0.1-SNAPSHOT"
  :description "Patients CRUD"
  :license {:name "GNU General Public License"
            :url "http://www.gnu.org/licenses/gpl.html"}
  :main patients.main
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [com.github.seancorfield/next.jdbc "1.2.780"]
                 [org.postgresql/postgresql "42.3.6"]
                 [environ "1.2.0"]
                 [http-kit "2.5.3"]
                 ]
  :profiles {:dev {:plugins [[lein-environ "0.4.0"]
                            ]
                   :dependencies []
                   :source-paths ["dev"]
                   :env {:db-url "jdbc:postgresql://localhost/patients_db?user=healthsamuraitest"
                        }
                  }
             :test {:plugins [[lein-environ "0.4.0"]]
                    :dependencies [[com.opentable.components/otj-pg-embedded "0.7.1"]
                                  ]
                    :source-paths ["dev"]
                    :env {:pg-embedded-port 59432
                          :db-url "jdbc:postgresql://localhost:59432/postgres?user=postgres"
                         }
                   }})
