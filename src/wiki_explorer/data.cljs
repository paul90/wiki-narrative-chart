(ns wiki-explorer.data
  "Contains functions for manipulating the application data"
  (:require [om.core :as om :include-macros true]))





;; Page:

(defn set-slug
  "Add the page slug to the application state"
  [appState pageSlug]

  (swap! appState update-in [:page] conj {:slug pageSlug}))

(defn set-title
  "Add the page tile to the application state"
  [appState pageTitle]

  (swap! appState update-in [:page] conj {:title pageTitle}))





;; Neighborhood:

(defn add-neighbor
  "Add a new Neighbor to the Neighborhood, if not already added"
  [appState newNeighbor]

  (if-not (contains? (:neighborhood @appState) newNeighbor)
    (do
      (swap! appState update-in [:neighborhood] conj {newNeighbor {:site newNeighbor :state "wait" :y (inc (count (:neighborhood @appState)))}})
      (swap! appState update-in [:processQueue] conj newNeighbor))))

(defn change-neighbor-state
  "Change the state of a neighbor"
  [appState site siteState]

  (swap! appState assoc-in [:neighborhood site :state] siteState))
