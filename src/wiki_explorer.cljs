(ns wiki-explorer.core
  (:refer-clojure :exclude [filter])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [wiki-explorer.data :as data]
            [wiki-explorer.render :as render]
            [wiki-explorer.journal :as journal])
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


;; initial state will eventually be loaded from data passed
;; in the url.
;
;; the page location is
;;     (.-location js/window)
;;
;; the page fragment can be extracted using closure Uri
;;     (.getFragment (Uri. (.-href (.-location js/window))))
;;
;;
;; But for now...
;;
;; some testdata using a local test wiki as the starting point
;;
;; page:         'chorus-of-voices'
;; neighborhood: localhost:3000
;;               ward.fed.wiki.org
;;               design.fed.wiki.org
;;               tug.fed.wiki.org

;(data/set-slug app-state "what-the-heck-do-we-call-fedwiki-pieces")
;(data/set-slug app-state "chorus-of-voices")
;(data/set-slug app-state "test")
;(data/set-slug app-state "the-hidden-history-of-online-learning")
(data/set-slug app-state "machines-and-software")

;(data/add-neighbor app-state "localhost:3000")
;(data/add-neighbor app-state "ward.fed.wiki.org")
;(data/add-neighbor app-state "design.fed.wiki.org")
;(data/add-neighbor app-state "tug.fed.wiki.org")
;(data/add-neighbor app-state "machines.hapgood.net")
(data/add-neighbor app-state "hhol.mike.fed.wiki.org")


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

;;(print (count (:mergedJournal @app-state)))



