(ns http-spec.spec-5-concurrency
  (:import (java.util Date)
           (java.text SimpleDateFormat))
  (:require
    [speclj.core :refer :all]
    [clj-http.client :as client]
    [http-spec.spec-helper :as helper]))

(def date-format (SimpleDateFormat. "yyyy-MM-dd HH:mm:ss"))

(describe "Concurrency"

  (before-all (helper/start-server "-p" "7654" "-r" "testroot"))
  (after-all (helper/stop-server))

  (it "/ping responds immediately"
    (let [now (Date.)
          response (client/get "http://localhost:7654/ping")
          body (:body response)]
      (should-contain "<h2>Ping</h2>" body)
      (should-contain (str "<li>start time: " (.format date-format now) "</li>") body)
      (should-contain (str "<li>end time: " (.format date-format now) "</li>") body)))

  (it "/ping/1 responds 1 second later"
    (let [now (Date.)
          later (Date. ^long (+ (.getTime now) 1000))
          response (client/get "http://localhost:7654/ping/1")
          body (:body response)]
      (should-contain "<h2>Ping</h2>" body)
      (should-contain (str "<li>start time: " (.format date-format now) "</li>") body)
      (should-contain (str "<li>end time: " (.format date-format later) "</li>") body)))

  (it "/ping/2 responds 2 seconds later"
    (let [now (Date.)
          later (Date. ^long (+ (.getTime now) 2000))
          response (client/get "http://localhost:7654/ping/2")
          body (:body response)]
      (should-contain "<h2>Ping</h2>" body)
      (should-contain (str "<li>start time: " (.format date-format now) "</li>") body)
      (should-contain (str "<li>end time: " (.format date-format later) "</li>") body)))

  (it "concurrent requests to /ping/1"
    (let [now (Date.)
          later (Date. ^long (+ (.getTime now) 1000))
          requests (take 3 (repeatedly (fn [] (future (client/get "http://localhost:7654/ping/1")))))
          responses (doall (pmap deref requests))
          elapsed (- (System/currentTimeMillis) (.getTime now))]
      ;(prn "elapsed: " elapsed)
      (should (< elapsed 1500))
      (doseq [response responses]
        (should-contain (str "<li>start time: " (.format date-format now) "</li>") (:body response))
        (should-contain (str "<li>end time: " (.format date-format later) "</li>") (:body response)))))

  (it "request for file while requesting /ping/1"
    (let [ping (future (client/get "http://localhost:7654/ping/1"))
          _ (Thread/sleep 100) ;; let ping request start running
          file (client/get "http://localhost:7654/img/autobot.jpg")]
      (should= 200 (:status @ping))
      (should= 200 (:status file))))

  )

