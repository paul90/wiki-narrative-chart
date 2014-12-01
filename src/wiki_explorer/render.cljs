(ns wiki-explorer.render
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

;; Render the bottom bar

(defn neighbor-view [neighbor owner]
  (reify
    om/IDisplayName
    (display-name [_]
                  "neighbor")
    om/IRender
    (render [this]
            (dom/span #js {:className "neighbor"}
                      (dom/div #js {:className (:state neighbor)}
                               (dom/img #js {:src (str "http://" (:site neighbor) "/favicon.png")
                                             :title (:site neighbor)}))))))


(defn neighborhood-view [state owner]
  (reify
    om/IDisplayName
    (display-name [_]
                  "neighborhood")
    om/IRender
    (render [this]

            (apply dom/section #js {:className "neighborhood"}
                   (om/build-all neighbor-view (map val (:neighborhood state)))))))


;; Render the page narrative chart

(defn narrative-view
  [state]
  (prn "in narrative-view")
  )

(defn data-changing
  [old new]
  (not= (:mergedJournal old) (:mergedJournal new)))


(defn neighborhood-narrative [state owner]
  (reify
    om/IDisplayName
    (display-name [_]
                  "neighborhood-narrative")
    om/IDidMount
    (did-mount [this]
               (if (not-empty (:mergedJournal state))
                 (narrative-view state)))
    om/IDidUpdate
    (did-update [this prev-props prev-state]
                (when (data-changing prev-props state)
                  (if (not-empty (:mergedJournal prev-props))
                    (.remove (.-firstChild (om/get-node owner "d3-node"))))
                  (narrative-view state)))
    om/IRender
    (render [this]
            (dom/div #js {:className "neighborhood-narrative"
                                :react-key "d3-node"
                                :ref "d3-node"
                                :id "d3-node"}))))
