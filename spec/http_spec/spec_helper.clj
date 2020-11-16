(ns http-spec.spec-helper
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [clojure.java.shell :as sh]
    [speclj.core :refer :all]
    [clojure.string :as str])
  (:import (java.io StringWriter IOException)
           (java.nio.charset Charset)))

(def config-atom (atom nil))
(def server-atom (atom nil))

(defn read-config []
  (edn/read-string (slurp (io/resource "config.edn"))))

(defn config []
  (when-not @config-atom (reset! config-atom (read-config)))
  @config-atom)

(defn run-server [& options]
  (apply sh/sh (concat (str/split (:cmd (config)) #" ") options)))

(defn- stream-to-string
  ([in] (stream-to-string in (.name (Charset/defaultCharset))))
  ([in enc]
   (with-open [bout (StringWriter.)]
     (try
       (io/copy in bout :encoding enc)
       (catch IOException e))
     (.toString bout))))

(defn start-server [& options]
  (assert (nil? @server-atom) "Can't start server.  A server is already running!")
  (let [command (concat (str/split (:cmd (config)) #" ") options)
        _ (when (:debug? (config)) (println "Starting server: " command))
        process (.exec (Runtime/getRuntime) ^"[Ljava.lang.String;" (into-array command) nil nil)]
    (with-open [stdout (.getInputStream process)
                stderr (.getErrorStream process)]
      (let [out (future (stream-to-string stdout))
            err (future (stream-to-string stderr))
            server {:out out :err err :process process}]
        (reset! server-atom server)
        (Thread/sleep (:startup-millis (config) 5000))
        ))))

(defn stop-server []
  (assert @server-atom "Can't stop server.  None running!")
  (let [server @server-atom
        ^Process process (:process server)]
    (.destroyForcibly process)
    (.waitFor process)
    (when (:debug? (config))
      (println "server stopped")
      (println "out: " @(:out server))
      (println "err: " @(:err server)))
    (reset! server-atom nil)))
