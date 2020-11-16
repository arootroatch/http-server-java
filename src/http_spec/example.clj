(ns http-spec.example
  (:require
    [compojure.core :refer [defroutes routes GET]]
    [compojure.route :as route]
    [org.httpkit.server :refer [run-server]]
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.cookies :refer [wrap-cookies]]
    [ring.middleware.flash :refer [wrap-flash]]
    [ring.middleware.head :refer [wrap-head]]
    [ring.middleware.keyword-params :refer [wrap-keyword-params]]
    [ring.middleware.multipart-params :refer [wrap-multipart-params]]
    [ring.middleware.nested-params :refer [wrap-nested-params]]
    [ring.middleware.not-modified :refer [wrap-not-modified]]
    [ring.middleware.params :refer [wrap-params]]
    [ring.middleware.resource :refer [wrap-resource]]
    [ring.middleware.file :refer [wrap-file]]
    [ring.middleware.session :refer [wrap-session]]
    [clojure.java.io :as io]))

(defroutes app
  (GET "/" [] "<h1>Hello World</h1>")
  (route/not-found "<h1>Page not found</h1>"))

(defn wrap-server-header [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc-in response [:headers "Server"] "Example Server"))))

(defn wrap-prn [handler]
  (fn [request]
    (let [response (handler request)]
      (prn response)
      response)))

(defn root-handler [app root]
  (-> app
      wrap-keyword-params
      wrap-multipart-params
      wrap-nested-params
      wrap-params
      (wrap-file root)
      wrap-content-type
      wrap-server-header
      wrap-head
      wrap-prn
      ))

(defn start [config]
  (run-server (root-handler app (:root config)) {:port (:port config)}))

(defmulti parse-arg (fn [config] (first (:args config))))

(defmethod parse-arg :default [config]
  (println "Invalid option: " (first (:args config)))
  (assoc config :usage? true :args nil :exit-code -1))

(defmethod parse-arg "-h" [config]
  (-> config
      (update :args rest)
      (assoc :usage? true)))

(defmethod parse-arg "-x" [config]
  (-> config
      (update :args rest)
      (assoc :exit? true)))

(defmethod parse-arg "-p" [config]
  (-> config
      (assoc :port (Integer/parseInt (second (:args config))))
      (update :args #(drop 2 %))))

(defmethod parse-arg "-r" [config]
  (let [path (second (:args config))
        root (io/file path)]
    (assert (.exists root) (str "Root path '" path "' doesn't exist"))
    (assert (.isDirectory root) (str "Root path '" path "' is not a directory"))
    (-> config
        (assoc :root (.getCanonicalPath root))
        (update :args #(drop 2 %)))))

(defn parse-args [config]
  (loop [config config]
    (if (seq (:args config))
      (recur (parse-arg config))
      config)))

(defn usage [config]
  (println "Usage: lein run -m http-spec.example [options]")
  (println "  -p     Specify the port.  Default is 80.")
  (println "  -r     Specify the root directory.  Default is the current working directory.")
  (println "  -h     Print this help message")
  (println "  -x     Print the startup configuration without starting the server")
  (System/exit (:exit-code config 0)))

(defn print-config [config]
  (println "Running on port:" (:port config))
  (println "Serving files from:" (:root config)))

(def default-config {:port 80 :root (.getCanonicalPath (io/file ".")) :usage? false :exit? false})

(defn -main [& args]
  (println "Example Server")
  (let [config (parse-args (assoc default-config :args args))]
    (when (:usage? config) (usage config))
    (print-config config)
    (when-not (:exit? config) (start config))))

