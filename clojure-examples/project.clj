(defproject clojure-examples "0.1.0-SNAPSHOT"
  :description "Examples demonstrating Functional Programming principles using Clojure"
  :url "https://github.com/yourusername/java21-vs-clojure"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/core.async "1.6.673"]
                 [org.clojure/core.match "1.0.1"]
                 [clojure.java-time "1.1.0"]
                 [http-kit "2.7.0"]
                 [cheshire "5.11.0"]
                 [org.clojure/java.jdbc "0.7.12"]
                 [org.clojure/tools.logging "1.2.4"]]

  :main ^:skip-aot clojure-examples.core
  :target-path "target/%s"

  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             :dev {:dependencies [[org.clojure/test.check "1.1.1"]
                                  [criterium "0.4.6"]]
                   :plugins [[lein-kibit "0.1.8"]
                             [jonase/eastwood "1.4.0"]]}}

  :repl-options {:init-ns clojure-examples.core})