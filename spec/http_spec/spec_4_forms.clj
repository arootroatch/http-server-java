(ns http-spec.spec-4-forms
  (:require
    [clj-http.client :as client]
    [http-spec.spec-helper :as helper]
    [speclj.core :refer :all]
    [clojure.java.io :as io]))

(describe "Forms"

  (before-all (helper/start-server "-p" "7654" "-r" "testroot"))
  (after-all (helper/stop-server))

  (it "/form handles get form"
    (let [response (client/get "http://localhost:7654/form?foo=1&bar=2")
          body (:body response)]
      (should-contain "<h2>GET Form</h2>" body)
      (should-contain "<li>foo: 1</li>" body)
      (should-contain "<li>bar: 2</li>" body)))

  (it "/form handles post multipart form with file upload"
    (let [response (client/post "http://localhost:7654/form"
                                {:multipart [{:name "file"
                                              :content-type "image/jpg"
                                              :content (io/file "testroot/img/autobot.jpg")}]})
          body (:body response)]
      (should-contain "<h2>POST Form</h2>" body)
      (should-contain "<li>file name: autobot.jpg</li>" body)
      (should-contain "<li>content type: application/octet-stream</li>" body)
      (should-contain "<li>file size: 58453</li>" body)))

  )





