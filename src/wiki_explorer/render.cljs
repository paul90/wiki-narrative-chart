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


(defn neighborhood-view [neighborhood owner]
  (reify
    om/IDisplayName
    (display-name [_]
                  "neighborhood")
    om/IRender
    (render [this]
            (apply dom/section #js {:className "neighborhood"}
                   (om/build-all neighbor-view neighborhood)))))


;; Render the page narrative chart



