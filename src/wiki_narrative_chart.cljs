(ns wiki-narrative-chart.core
  (:refer-clojure :exclude [filter])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cuerdas.core :as str]
            [wiki-narrative-chart.data :as data]
            [wiki-narrative-chart.render :as render]
            [wiki-narrative-chart.journal :as journal])
  (:import [goog Uri]))

(enable-console-print!)

;; ## Application State
;;
;;

(def app-state (atom {:page {}
                      :neighborhood {}
                      :mergedJournal []
                      :processQueue cljs.core.PersistentQueue.EMPTY
                      :window-refresh 0}))


;; we expect the page location to contain the page slug followed by a list of one or more sites, each
;; site prefixed by a '@'.

(let [slug (first (str/split (.getFragment (Uri. (.-href (.-location js/window)))) "@" ))
      sites (rest (str/split (.getFragment (Uri. (.-href (.-location js/window)))) "@" ))]

  (data/set-slug app-state slug)

  (doseq [site sites]
    (data/add-neighbor app-state site)))


;; we render a status bar with the site icons for the sites in the neighborhood. Their state om/will
;; change as they are processed, but reflecting this in the interface just happens.
(om/root
   render/neighborhood-view
   app-state
   {:target (. js/document (getElementById "neighborhood"))})

;; we render the page neighborhood narrative using the journal entries we build-up in the `:mergedJournal`
;; the view should get updated as the merged journal grows.
;;

(om/root
   render/neighborhood-narrative
   app-state
   {:target (. js/document (getElementById "neighborhood-narrative"))})



;; We now need to build the merged journal for the page in the neighborhood
;;
;; We need to take each site in the `:processQueue`, fetch the page from that site
;; and process the journal into the form we want, and merge it into the growning `:mergedJournal`
;;

(journal/build-neighborhood-journal app-state)

;;(print (:neighborhood @app-state))
;;(print @app-state)

; (:mergedJournal @app-state)

;(conj (vec (:mergedJournal @app-state)) {:type "last"})

