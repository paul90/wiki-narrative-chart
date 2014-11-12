(ns wiki-explorer.data
  "Contains functions for manipulating the application data")





; Page:







; Neighborhood:

(defn add-neighbour
  [state newNeighbour]
  (swap! state update-in [:neighborhood] conj newNeighbour)
  (swap! state update-in [:processQueue] conj (:site newNeighbour)))



; Merged Journal:




