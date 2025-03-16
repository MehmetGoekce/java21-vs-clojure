(defproject clojure-examples "0.1.0-SNAPSHOT"
  :description "Clojure Functional Programming Examples"
  :url "https://github.com/yourusername/java21-vs-clojure"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/core.async "1.6.673"]
                 [org.clojure/core.match "1.0.1"]
                 [clojure.java-time "1.1.0"]
                 [http-kit "2.7.0"]
                 [cheshire "5.11.0"]]
  :main ^:skip-aot clojure-examples.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             :dev {:dependencies [[org.clojure/test.check "1.1.1"]]}}
  :repl-options {:init-ns clojure-examples.core})