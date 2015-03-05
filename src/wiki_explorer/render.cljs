(ns wiki-explorer.render
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async :refer [put! chan <!]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.events :as events]
            [cuerdas.core :as str]))

;; ### Render the page footer - neighborhood bar


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


;; ### Render the page narrative chart

;; we need to keep track of some details about each site:
;; * the colour of the line we are drawing,
;; * the end of the line we are drawing.
;;
(def ^:dynamic site {})

;; tracking the size of the area for the narrative chart - so we can redraw if/when the page is
;; resized

(def ^:dynamic page-size {})

(defn set-page-size
  []
  (def page-size {:margin 60
                  :width (.. js/document (getElementById "d3-node") -offsetWidth)
                  :height (.. js/document (getElementById "d3-node") -offsetHeight)}))

(def last-points (atom {}))

(defn narrative-view
  [state]

  (set-page-size)
  (reset! last-points)
  (let [m (:margin page-size)

        h (- (:height page-size) (* m 2))
        w (- (:width page-size) (* m 2))

        n (inc (count (:neighborhood state)))


        startDate (js/Date. (:date (first (:mergedJournal state))))
        endDate (js/Date. (:date (last (:mergedJournal state))))


        x-scale (.. js/d3 -time scale
                    (range #js [0, w])
                    (domain #js [startDate endDate])
                    (nice))

        x-axis (.. js/d3 -svg axis
                   (scale x-scale)
                   (orient "bottom"))

        y-scale (.. js/d3 -scale linear
                    (range #js [(+ 0 m) (- h m)])
                    (domain #js [n 0]))


        svg (.. js/d3 (select "#d3-node")
                (append "svg")
                (attr #js {:width (+ w (* 2 m)) :height (+ h (* 2 m))}))


        g (.. svg (append "g")
              (attr #js {:transform (str "translate(" m "," m ")")}))]

    ;; x-axis
    (.. g (append "g")
        (attr #js {:class "x axis"})
        (attr #js {:transform (str "translate(0," h ")")} )
        (call x-axis))

    (loop [journalData (:mergedJournal state)]
      (when (not (empty? journalData))

        (.. g (append "circle")
            (attr #js {"cx" (x-scale (:date (first journalData)))})
            (attr #js {"cy" (y-scale (- (:y (val (find (:neighborhood state) (str/strip-suffix (:site (first journalData)) ":local"))))
                                        (if (str/endswith? (:site (first journalData)) ":local") 0.25)))})
            (attr #js {"r" 3}))



        (if (get @last-points (:site (first journalData)))
          (.. g (append "path")
              (attr #js {"d" (str "M" (:x (get @last-points (:site (first journalData)))) "," (:y (get @last-points (:site (first journalData))))
                                  "L" (x-scale (:date (first journalData))) "," (y-scale (- (:y (val (find (:neighborhood state) (str/strip-suffix (:site (first journalData)) ":local"))))
                                                                                            (if (str/endswith? (:site (first journalData)) ":local") 0.25))))})
              (attr #js {"style" "stroke: black; stroke-width: 1px"})))

        (if (:fork-from (first journalData))
          (.. g (append "path")
              (attr #js {"d" (str "M" (:x (get @last-points (:fork-from (first journalData)))) "," (:y (get @last-points (:fork-from (first journalData))))
                                  "L" (x-scale (:date (first journalData))) "," (y-scale (- (:y (val (find (:neighborhood state) (str/strip-suffix (:site (first journalData)) ":local"))))
                                                                                            (if (str/endswith? (:site (first journalData)) ":local") 0.25))))})
              (attr #js {"style" "stroke: red; stroke-width: 1px"})))


        (swap! last-points assoc-in [(:site (first journalData))] {:x (x-scale (:date (first journalData)))
                                                                   :y (y-scale (- (:y (val (find (:neighborhood state) (str/strip-suffix (:site (first journalData)) ":local"))))
                                                                                  (if (str/endswith? (:site (first journalData)) ":local") 0.25)))})

        (recur (rest journalData))))






  ))


(defn window-resize [state]
  (.addEventListener
   js/window "resize"
   (fn []
     (om/transact! state
                   #(assoc % :window-refresh (inc (:window-refresh @state)))))))



(defn data-changing
  [old new]
  (or
   (not= (:mergedJournal old) (:mergedJournal new))
   (not= (:window-refresh old) (:window-refresh new))))


(defn neighborhood-narrative [state owner]
  "D3 / Om  integration"
  (reify
    om/IDisplayName
    (display-name [_]
                  "neighborhood-narrative")

    om/IWillMount
    (will-mount [_]
                (window-resize state))

    om/IDidMount
    (did-mount [this]
               ;; don't do anything if the merged journal is empty
               (if (not-empty (:mergedJournal state))
                 (narrative-view state)))

    om/IDidUpdate
    (did-update [this prev-props prev-state]
                ;; only update if the merged journal has changed
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



;; below here is a scratch pad for testing things...



