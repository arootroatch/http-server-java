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
       (catch IOException e
         (.write bout "closed\n")
         (.close bout)))
     (.toString bout))))

(defn copy-output [stream writer]
  (try
    (io/copy stream writer)
    (catch IOException e
      (.write writer "closed\n")
      (.close writer))))

(defn copy-thread [stream writer]
  (let [t (Thread. ^Runnable (fn [] (copy-output stream writer)))]
    (.start t)
    t))

(defn- do-start-server [& options]
  (assert (nil? @server-atom) "Can't start server.  A server is already running!")
  (let [command (concat (str/split (:cmd (config)) #" ") options)
        _ (when (:debug? (config)) (println "Starting server: " command))
        process (.exec (Runtime/getRuntime) ^"[Ljava.lang.String;" (into-array command) nil nil)]
    (let [stdout (.getInputStream process)
          stderr (.getErrorStream process)
          out (StringWriter.)
          out-thread (copy-thread stdout out)
          err (StringWriter.)
          err-thread (copy-thread stderr err)
          server {:out     out :out-thread out-thread
                  :err     err :err-thread err-thread
                  :process process}]
      (reset! server-atom server)
      (Thread/sleep (:startup-millis (config) 5000)))))

(defn start-server [& options] (apply do-start-server options))

(defn print-output [server]
  (when (:debug? (config))
    (println "out:")
    (println (.toString (:out server)))
    (println "err:")
    (println (.toString (:err server)))))

(defn- do-stop-server []
  (assert @server-atom "Can't stop server.  None running!")
  (Thread/yield)
  (let [server @server-atom
        ^Process process (:process server)]
    (.destroyForcibly process)
    (.waitFor process)
    (.join (:out-thread server))
    (.join (:err-thread server))
    (print-output server)
    (reset! server-atom nil)))

(defn stop-server [] (do-stop-server))
