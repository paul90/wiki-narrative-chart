(defproject wiki-explorer "0.1.0-SNAPSHOT"
  :description "Federated Wiki Explorer: explore a page's twins in a federated wiki neighbourhood, and how they related."
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [om "0.7.3"]]

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
