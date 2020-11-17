(ns http-spec.example
  (:import (java.util Date)
           (java.text SimpleDateFormat))
  (:require
    [compojure.core :refer [defroutes routes GET POST]]
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
    [clojure.java.io :as io]
    [clojure.string :as str]))

(defn wrap-list [items] (str "<ul>" items "</ul>"))

(def date-format (SimpleDateFormat. "yyyy-MM-dd HH:mm:ss"))

(defn ping [seconds]
  (let [start (Date.)]
    (Thread/sleep (* seconds 1000))
    (str "<h2>Ping</h2>"
         "<ul>"
         "<li>start time: " (.format date-format start) "</li>"
         "<li>end time: " (.format date-format (Date.)) "</li>"
         "<li>sleep seconds: " seconds "</li>"
         "</ul>")))

(defn get-form [request]
  (str "<h2>GET Form</h2>"
       (->> (:params request)
            (map (fn [[k v]] (str "<li>" (name k) ": " v "</li>")))
            (apply str)
            wrap-list)))

(defn post-form [request]
  (let [upload (-> request :params :file)]
    (str "<h2>POST Form</h2>"
         "<ul>"
         "<li>file name: " (:filename upload) "</li>"
         "<li>content type: " (:content-type upload) "</li>"
         "<li>file size: " (:size upload) "</li>"
         "</ul>")))

(defn file-link [root parent child]
  (let [file (io/file root parent child)
        path (str "/" (if (str/blank? parent) child (str parent "/" child)))]
    (if (.isDirectory file)
      (str "<li><a href=\"/listing" path "\">" child "</a></li>")
      (str "<li><a href=\"" path "\">" child "</a></li>"))))

(defn listing [root path]
  (let [dir (io/file root path)]
    (when (and (.exists dir) (.isDirectory dir))
      (->> (.list dir)
           sort
           (map (partial file-link root path))
           (apply str)
           wrap-list))))

(defn app [root]
  (routes
    (GET "/listing" [] (listing root ""))
    (GET "/listing/:path" [path] (listing root path))
    (GET "/form" request (get-form request))
    (POST "/form" request (post-form request))
    (GET "/ping" [] (ping 0))
    (GET "/ping/:seconds" [seconds] (ping (Integer/parseInt seconds)))
    (route/not-found "<h1>Page not found</h1>")))

(defn wrap-server-header [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc-in response [:headers "Server"] "Example Server"))))

;(defn wrap-prn [handler]
;  (fn [request]
;    (let [response (handler request)]
;      (prn response)
;      response)))

(defn wrap-log-requests [handler]
  (let [counter (atom 0)]
    (fn [request]
      (let [request (assoc request :id (swap! counter inc))]
        (println (str/join "\t" [(str (:id request) ">") (:request-method request) (:uri request) (:query-string request) (:body request)]))
        (let [response (handler request)]
          (println (str/join "\t" [(str (:id request) "<") (:status response) (count (str (:body response)))]))
          (.flush System/out)
          response)))))

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
      wrap-log-requests
      ))

(defn start [config]
  (let [root (:root config)]
    (run-server (root-handler (app root) root) {:port (:port config)})))

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
    (try
      (when-not (:exit? config)
        (start config))
      (catch Throwable e
        (.printStackTrace e)))))

