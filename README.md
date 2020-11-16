# HTTP Server Challenge Specifications

Executable specification for Micah's HTTP Server Challenge.  These specs are written in Clojure but apply to servers 
written in any language.

### Rules of Engagement:
 1) Use the [HTTP 1.1 RFC](https://tools.ietf.org/html/rfc7231), [HTTP 2 RFC](https://tools.ietf.org/html/rfc7540), or 
 [Wikipedia HTTP Page](https://en.wikipedia.org/wiki/Hypertext_Transfer_Protocol) as references.
 2) DO NOT STUDY EXISTING HTTP CODE
 3) NO 3rd PARTY LIBRARIES

###  Spec Setup

#### 1) [Leiningen](https://leiningen.org/) must be installed.
 
#### 2) Configuration

The specs must be configured to run your server implementation.  

#### 3) Execution

From within this `http-spec` directory:

    lein run my-config.edn  
    
    
### Specification Categories
