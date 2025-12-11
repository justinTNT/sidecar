(ns sidecar.repl
  (:require [nrepl.server :as nrepl]
            [sidecar.core :as core]))

;; Sidecar brain REPL server on port 7888
;; Provides interactive development environment for disposable tools

(defonce server (atom nil))

(defn start-server []
  (when-not @server
    (reset! server (nrepl/start-server :port 7888 :bind "127.0.0.1"))
    (println "[sidecar] nREPL server started on port 7888")
    (println "[sidecar] Connect with: lein repl :connect 7888")
    (println "[sidecar] Or: clj -M:nrepl")))

(defn stop-server []
  (when @server
    (nrepl/stop-server @server)
    (reset! server nil)
    (println "[sidecar] nREPL server stopped")))

(defn check []
  (println "[sidecar] Running all checks...")
  (core/run-all))

(defn help []
  (println "
Sidecar Brain Commands:
  (check)    - Run all consistency checks (includes test harness)
  (help)     - Show this help message

Always-on test harness:
  (require '[sidecar.test-harness :as tests])
  (tests/run-all)                     ; Run all Clojure-modeled tests
  (tests/list-tests)                  ; List all available tests by suite
  (tests/explain-test \"test-name\")     ; Get detailed explanation
  (tests/find-tests-by-keyword \"keyword\")  ; Search tests by keyword

Domain knowledge oracle:
  (require '[sidecar.domain-knowledge :as dk])
  (dk/query :property-types)           ; List property types
  (dk/query :listing-types)            ; Listing type set
  (dk/query :features)                 ; Property features
  (dk/query :attributes)               ; Listing attributes
  (dk/query :constraints)              ; All validation constraints
  (dk/query :endpoints)                ; API endpoint map

Search parameter validation:
  (require '[sidecar.validator :as v])
  (v/validate-search-params params)    ; Validate search params
  (v/validate-range {:min 100 :max 200} \"Price\")  ; Validate a range
  (v/validate-pagination 1 25)         ; Validate page/pageSize
  (v/validate-coordinates -33.8688 151.2093)  ; Validate lat/lon
  (v/validate-location location-map)   ; Validate location structure

Other harnesses (stubs):
  (require '[sidecar.z3-harness :as z3])
  (z3/run-all)        ; SMT constraint checks

  (require '[sidecar.datalog-harness :as datalog])
  (datalog/run-all)   ; Datalog relationship checks

  (require '[sidecar.logic-harness :as logic])
  (logic/run-all)     ; Logic programming search/generation
"))

(defn -main [& args]
  (start-server)
  ;; Keep the process alive
  @(promise))

