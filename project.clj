(defproject wiki-narrative-chart "0.1.1"
  :description "Federated Wiki Narrative Chart: explore a page's twins in a federated wiki neighbourhood, and how they related."

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2760"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.omcljs/om "0.9.0"]
                 [cljs-http "0.1.36"]
                 [cuerdas "0.3.2"]]

  :plugins [[lein-cljsbuild "1.0.3"]]

  :license {:name "MIT"
            :url "https://github.com/paul90/wiki-narrative-chart/blob/master/LICENSE.txt"}

  :source-paths ["src"]

  :cljsbuild {
    :builds {:dev
             {:id "dev"
              :source-paths ["src"]
              :compiler {:output-to "dev/client/narrative.js"
                         :output-dir "dev/client/out"
                         :optimizations :none
                         :source-map true}}
             :prod
             {:id "release"
              :source-paths ["src"]
              :compiler {:main wiki-narrative-chart.core
                         :output-to "dist/client/narrative.js"
                         :output-dir "dist/client/target/"
                         :optimizations :advanced
                         :pretty-print false}}}})
