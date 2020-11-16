(ns http-spec.spec-2-basics
  (:import (clojure.lang ExceptionInfo))
  (:require
    [clj-http.client :as client]
    [http-spec.spec-helper :as helper]
    [speclj.core :refer :all]
    ))

(describe "Basics"

  (before-all (helper/start-server "-p" "7654" "-r" "testroot"))
  (after-all (helper/stop-server))

  (it "response to '' with index.html"
    (let [response (client/get "http://localhost:7654")]
      (should= 200 (:status response))
      (should-contain "<h1>Hello, World!</h1>" (:body response))))

  (it "response to '/' with index.html"
    (let [response (client/get "http://localhost:7654/")]
      (should-contain "<h1>Hello, World!</h1>" (:body response))))

  (it "response to garbage request is 404"
    (should-throw ExceptionInfo
                  "clj-http: status 404"
                  (client/get "http://localhost:7654/blah")))

  (it "contains 'Server' header"
    (let [response (client/get "http://localhost:7654/")]
      (should= (:name (helper/config)) (get-in response [:headers "Server"]))))

  )



