(defproject patients "0.0.1-SNAPSHOT"
  :description "Patients CRUD"
  :license {:name "GNU General Public License"
            :url "http://www.gnu.org/licenses/gpl.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.postgresql/postgresql "42.3.6"]
                 ]
  :profiles {:dev {:plugins []
                   :dependencies []
                   :source-paths ["dev"]}})
