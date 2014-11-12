(ns wiki-explorer.core
  (:refer-clojure :exclude [filter])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [wiki-explorer.data :as data]
            [wiki-explorer.render :as render]))

(enable-console-print!)

; define application state

(def app-state (atom {:page {}
                      :neighborhood []
                      :mergedJournal []
                      :processQueue cljs.core.PersistentQueue/EMPTY}))


; initial state will eventually be loaded from data passed
; in the url. But...

; some testdata using a local test wiki as the starting point

; page:         'chorus-of-voices'
; neighborhood: localhost:3000
;               ward.fed.wiki.org
;               design.fed.wiki.org
;               tug.fed.wiki.org

(data/add-neighbour app-state {:site "localhost:3000" :state "wait"})
(data/add-neighbour app-state {:site "ward.fed.wiki.org" :state "wait"})
(data/add-neighbour app-state {:site "design.fed.wiki.org" :state "wait"})
(data/add-neighbour app-state {:site "tug.fed.wiki.org" :state "wait"})




(om/root render/neighborhood-view (:neighborhood @app-state)
         {:target (. js/document (getElementById "neighborhood"))})







