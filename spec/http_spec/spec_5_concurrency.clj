(ns http-spec.spec-5-concurrency
  (:require [speclj.core :refer :all]))

(describe "Concurrency"

  (xit "/ping responds immediately")
  (xit "/ping/1 responds 1 second later")
  (xit "/ping/2 responds 2 seconds later")
  (xit "concurrent requests to /ping/1")
  (xit "request for file while requesting /ping/1")

)
