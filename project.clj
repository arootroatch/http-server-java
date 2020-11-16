(defproject http-spec "0.1.0-SNAPSHOT"
  :description "Micah's HTTP Server Challenge Spec"
  :url "https://github.com/slagyr/http-spec"
  :license {:name "GNU GENERAL PUBLIC LICENSE"}
  :main http-spec.core
  :dependencies [
                 [clj-http "3.10.1"]
                 [compojure "1.6.1" :exclusions [ring/ring-core ring/ring-codec]]
                 [hiccup "1.0.5"]
                 [http-kit "2.3.0"]
                 [org.clojure/clojure "1.10.1"]
                 [ring/ring "1.8.1"]
                 ]
  :profiles {:dev {:dependencies [[speclj "3.3.2"]]}}
  :plugins [[speclj "3.3.2"]]
  :test-paths ["spec"])
