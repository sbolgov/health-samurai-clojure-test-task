(defproject patients "0.0.1-SNAPSHOT"
  :description "Patients CRUD"
  :license {:name "GNU General Public License"
            :url "http://www.gnu.org/licenses/gpl.html"}
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :main patients.main
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.11.4"]
                 [com.github.seancorfield/next.jdbc "1.2.780"]
                 [org.postgresql/postgresql "42.3.6"]
                 [environ "1.2.0"]
                 [http-kit "2.5.3"]
                 [compojure "1.6.1"]
                 [hiccup "1.0.5"]
                 [cljs-ajax "0.8.4"]
                 ]
  :cljsbuild {
    :builds [{:source-paths ["src" "dev"]
              :compiler {:output-to "target/classes/public/app.js"
                         :output-dir "target/classes/public/out"
                         :optimizations :none
                         :recompile-dependents true
                         :source-map true
                         :pretty-print true}}]}
  :profiles {:dev {:plugins [[lein-environ "0.4.0"]
                             [lein-cljsbuild "1.1.8"]
                            ]
                   :dependencies []
                   :source-paths ["dev"]
                   :env {:db-url "jdbc:postgresql://localhost/patients_db?user=healthsamuraitest"
                         :port 8080}
                  }
             :uberjar {:hooks [leiningen.cljsbuild]}
             :test {:plugins [[lein-environ "0.4.0"]]
                    :dependencies [[com.opentable.components/otj-pg-embedded "0.7.1"]
                                  ]
                    :source-paths ["dev"]
                    :env {:pg-embedded-port 59432
                          :db-url "jdbc:postgresql://localhost:59432/postgres?user=postgres"
                         }
                   }})
