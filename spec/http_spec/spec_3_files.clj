(ns http-spec.spec-3-files
  (:require
    [clj-http.client :as client]
    [http-spec.spec-helper :as helper]
    [speclj.core :refer :all]
    [clojure.java.io :as io]))

(describe "Files"

  (before-all (helper/start-server "-p" "7654" "-r" "testroot"))
  (after-all (helper/stop-server))

  (it "/listing gives a listing of files in the root directory"
    (let [response (client/get "http://localhost:7654/listing")
          body (:body response)]
      (should-contain #"<ul>.*</ul>" body)
      (should-contain "<li><a href=\"/index.html\">index.html</a></li>" body)
      (should-contain "<li><a href=\"/hello.pdf\">hello.pdf</a></li>" body)
      (should-contain "<li><a href=\"/listing/img\">img</a></li>" body)))

  (it "/listing/img gives a listing of files in the img directory"
    (let [response (client/get "http://localhost:7654/listing/img")
          body (:body response)]
      (should-contain #"<ul>.*</ul>" body)
      (should-contain "<li><a href=\"/img/autobot.jpg\">autobot.jpg</a></li>" body)
      (should-contain "<li><a href=\"/img/autobot.png\">autobot.png</a></li>" body)
      (should-contain "<li><a href=\"/img/decepticon.jpg\">decepticon.jpg</a></li>" body)
      (should-contain "<li><a href=\"/img/decepticon.png\">decepticon.png</a></li>" body)))

  (it "serves .html files"
    (let [response (client/get "http://localhost:7654/index.html")]
      (should= (slurp (io/file "testroot/index.html")) (:body response))
      (should= "text/html" (get-in response [:headers "Content-Type"]))))

  (it "serves .jpg files"
    (let [response (client/get "http://localhost:7654/img/autobot.jpg")]
      (should= (slurp (io/file "testroot/img/autobot.jpg")) (:body response))
      (should= "image/jpeg" (get-in response [:headers "Content-Type"]))))

  (it "serves .png files"
    (let [response (client/get "http://localhost:7654/img/decepticon.png")]
      (should= (slurp (io/file "testroot/img/decepticon.png")) (:body response))
      (should= "image/png" (get-in response [:headers "Content-Type"]))))

  (it "serves .pdf files"
    (let [response (client/get "http://localhost:7654/hello.pdf")]
      (should= (slurp (io/file "testroot/hello.pdf")) (:body response))
      (should= "application/pdf" (get-in response [:headers "Content-Type"]))))

)
