(ns http-spec.spec-helper
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [clojure.java.shell :as sh]
    [speclj.core :refer :all]
    [clojure.string :as str]))

(def config-atom (atom nil))

(defn read-config []
  (edn/read-string (slurp (io/resource "config.edn"))))

(defn config []
  (when-not @config-atom (reset! config-atom (read-config)))
  @config-atom)

(defn run-server [& options]
  (apply sh/sh (concat (str/split (:cmd (config)) #" ") options)))
