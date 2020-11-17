(ns http-spec.spec-4-forms
  (:require
    [clj-http.client :as client]
    [http-spec.spec-helper :as helper]
    [speclj.core :refer :all]
    ))

(describe "Forms"

  (before-all (helper/start-server "-p" "7654" "-r" "testroot"))
  (after-all (helper/stop-server))

  (it "/form handles get form"
    (let [response (client/get "http://localhost:7654/form?foo=1&bar=2")
          body (:body response)]
      (Thread/sleep 1000)
      (prn "response: " response)
      (should-contain "<h2>GET Form</h2>" body)
      (should-contain "<li>foo: 1</l1>" body)
      (should-contain "<li>bar: 2</l1>" body)))

  (xit "/form handles post form")


  )

