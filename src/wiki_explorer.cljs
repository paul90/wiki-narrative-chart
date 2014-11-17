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
                      :neighborhood []
                      :mergedJournal []
                      :processQueue cljs.core.PersistentQueue.EMPTY}))


; initial state will eventually be loaded from data passed
; in the url.

; the page location is
;     (.-location js/window)
;
; the page fragment can be extracted using closure Uri
;     (.getFragment (Uri. (.-href (.-location js/window))))



;But for now...

; some testdata using a local test wiki as the starting point

; page:         'chorus-of-voices'
; neighborhood: localhost:3000
;               ward.fed.wiki.org
;               design.fed.wiki.org
;               tug.fed.wiki.org

(data/set-slug app-state {:slug "chorus-of-voices"})

(data/add-neighbour app-state {:site "localhost:3000" :state "wait"})
(data/add-neighbour app-state {:site "ward.fed.wiki.org" :state "wait"})
(data/add-neighbour app-state {:site "design.fed.wiki.org" :state "wait"})
(data/add-neighbour app-state {:site "tug.fed.wiki.org" :state "wait"})




(om/root render/neighborhood-view (:neighborhood @app-state)
         {:target (. js/document (getElementById "neighborhood"))})


; om/root for the story narrative gets added here...


