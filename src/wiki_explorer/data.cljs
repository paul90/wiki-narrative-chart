(ns wiki-explorer.data
  "Contains functions for manipulating the application data")





;; Page:

(defn set-slug
  "Add the page slug to the application state"
  [state pageSlug]

  (swap! state update-in [:page] conj {:slug pageSlug}))

;  (set! (.-title js/document) "Chorus of Voices")





;; Neighborhood:

(defn add-neighbour
  "Add a new Neighbor to the Neighborhood, if not already added"
  [state newNeighbour]

  (swap! state update-in [:neighborhood] conj newNeighbour)
  (swap! state update-in [:processQueue] conj (:site newNeighbour)))


