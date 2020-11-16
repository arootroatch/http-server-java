(ns http-spec.spec-1-command-line
  (:require [speclj.core :refer :all]))

;; The HTTP server behaves like a traditional command line tool with options.
;;
;; e.g. The command name to start the server is 'my-http-server', the -help option should print the following, perhaps
;;   with additional options:
;;
;; Usage: my-http-server [options]
;;   -p     Specify the port.  Default is 80.
;;   -r     Specify the root directory.  Default is the current working directory.
;;   -h     Print this help message
;;   -x     Print the startup configuration without starting the server
;;
;; Upon startup, the server should print it's startup configuration
;;
;; HTTP Server
;; Running on port: <port>
;; Serving files from: <dir>


(describe "Command Line"

  (xit "--help prints usage without starting server")
  (xit "-x argument prints configuration without starting server")
  (xit "default port is 80")
  (xit "default root is current directory")
  (xit "port can be set using the -p argument")
  (xit "root can be set using the -r argument")

)
