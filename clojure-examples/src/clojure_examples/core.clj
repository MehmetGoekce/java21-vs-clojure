(ns clojure-examples.core
  (:require [clojure-examples.fundamentals :as fundamentals]
            [clojure-examples.patterns :as patterns]
            [clojure-examples.concurrency :as concurrency]
            [clojure-examples.dataprocessing :as dataprocessing]
            [clojure-examples.webscraper :as webscraper]
            [clojure-examples.ecommerce :as ecommerce])
  (:gen-class))

(defn run-all-examples []
  (println "\n=== Running Fundamental FP Examples ===")
  (fundamentals/demo)

  (println "\n=== Running Functional Design Patterns ===")
  (patterns/demo)

  (println "\n=== Running Concurrency Examples ===")
  (concurrency/demo)

  (println "\n=== Running Data Processing Example ===")
  (dataprocessing/-main)

  (println "\n=== Running Web Scraper Example ===")
  (webscraper/-main)

  (println "\n=== Running E-Commerce System Example ===")
  (ecommerce/demo))

(defn -main
  "Run examples based on arguments or run them all"
  [& args]
  (if (empty? args)
    (run-all-examples)
    (case (first args)
      "fundamentals" (fundamentals/demo)
      "patterns" (patterns/demo)
      "concurrency" (concurrency/demo)
      "dataprocessing" (dataprocessing/-main)
      "webscraper" (webscraper/-main)
      "ecommerce" (ecommerce/demo)
      (println "Unknown example. Available options: fundamentals, patterns, concurrency, dataprocessing, webscraper, ecommerce")))
  (System/exit 0))