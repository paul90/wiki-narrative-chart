(defproject wiki-explorer "0.1.0-SNAPSHOT"
  :description "Federated Wiki Explorer: explore a page's twins in a federated wiki neighbourhood, and how they related."

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [om "0.7.3"]
                 [cljs-http "0.1.20"]
                 [com.cognitect/transit-cljs "0.8.192"]]

  :plugins [[lein-cljsbuild "1.0.3"]]

  :source-paths ["src"]

  :cljsbuild {
    :builds [{:id "wiki-explorer"
              :source-paths ["src"]
              :compiler {
                :output-to "wiki_explorer.js"
                :output-dir "out"
                :optimizations :none
                :source-map true}}]})
