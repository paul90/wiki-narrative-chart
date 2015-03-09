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
            (dom/div #js {:className "neighbor"}
                     (dom/img #js {:className (:state neighbor)
                                   :src (str "http://" (:site neighbor) "/favicon.png")
                                   :title (:site neighbor)})))))


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

;; tracking the size of the area for the narrative chart - so we can redraw if/when the page is
;; resized

(def ^:dynamic page-size {})

(defn set-page-size
  []
  (def page-size {:margin 20
                  :width (.. js/document (getElementById "neighborhood-narrative") -offsetWidth)
                  :height (.. js/document (getElementById "neighborhood-narrative") -offsetHeight)}))

; tracking the last point we drawn for each site in the neighbourhood. So, we know where to
; draw the next point from.

(def last-points (atom {}))

(def site-colors ["#1f77b4" "#ff7f0e" "#2ca02c" "#d62728" "#9467bd" "#8c564b" "#e377c2" "#7f7f7f" "#bcbd22" "#17becf"])

(def local-colors ["#aec7e8" "#ffbb78" "#98df8a" "#ff9896" "#c5b0d5" "#c49c94" "#f7b6d2" "#c7c7c7" "#dbdb8d" "#9edae5"])

(defn index-of
  "returns the position of item within a collection"
  [coll v]
  (let [i (count (take-while #(not= v %) coll))]
    (when (or (< i (count coll))
            (= v (last coll)))
      i)))

(defn window-resize [state]
  (.addEventListener
   js/window "resize"
   (fn []
     (om/transact! state
                   #(assoc % :window-refresh (inc (:window-refresh @state)))))))




(defn render-journalEntry [state owner]
  (reify
    om/IDisplayName
    (display-name [_]
                  "journalEntry")

    om/IRender
    (render [this]

            (let [x (:x state)
                  y (:y state)

                  lastX (:x (get @last-points (:site state)))
                  lastY (:y (get @last-points (:site state)))

                  forkX (if (nil? (:fork-from state))
                          (:x (get @last-points (str (:site state) ":local")))
                          (:x (get @last-points (:fork-from state))))
                  forkY (if (nil? (:fork-from state))
                          (:y (get @last-points (str (:site state) ":local")))
                          (:y (get @last-points (:fork-from state))))]



              (swap! last-points assoc-in [(:site state)] {:x x :y y})

              (dom/g nil
                     (dom/circle #js {:cx x
                                      :cy y
                                      :r 3
                                      :stroke "black"})

                     (if (= (:type state) "fork")
                       (if (number? forkX)
                         (dom/path #js {:d (str "M" forkX "," forkY
                                                "C" (+ (* forkX (- 1 0.5)) (* x 0.5)) "," forkY
                                                " " (+ (* forkX (- 1 0.5)) (* x 0.5)) "," y
                                                " " x "," y)
                                        :fill "none"
                                        :stroke (:color state)
                                        :strokeWidth "2px"})))

                     (if (number? lastX)

                         (dom/line #js {:x1 lastX
                                        :y1 lastY
                                        :x2 (:x state)
                                        :y2 (:y state)
                                        :stroke (:color state)
                                        :strokeWidth "2px"})
                       (dom/text #js {:x (- x (:margin page-size))
                                      :y (- y 10)}
                                 (:site state))))))))




(defn calc-x-pos [je state]
  (+ (:margin page-size)
     (* (inc (index-of state je))
        (/ (- (:width page-size)
              (* 2 (:margin page-size)))
           (inc (count state))))))

(defn calc-y-pos [je state]
  (- (+ (:margin page-size)
        (* (- (:y (val (find state (str/strip-suffix (:site je) ":local"))))
              (if (str/endswith? (:site je) ":local") 0.25))
           (/ (- (:height page-size)
                 (* 2 (:margin page-size)))
              (inc (count state)))))
     (if (str/endswith? (:site je) ":local") 0.25)))

(defn calc-color [je state]
  (if (str/endswith? (:site je) ":local")
    (nth local-colors (mod (:y (val (find state (str/strip-suffix (:site je) ":local")))) 10))
    (nth site-colors  (mod (:y (val (find state (str/strip-suffix (:site je) ":local")))) 10))))


(defn neighborhood-narrative [state owner]
  "Drawing an SVG rendering of the merged page journals."
  (reify
    om/IDisplayName
    (display-name [_]
                  "neighborhood-narrative")

    om/IWillMount
    (will-mount [_]
                (window-resize state))

    om/IRender
    (render [this]

            (set-page-size)
            (reset! last-points)

            (apply dom/svg #js {:width  (:width page-size)
                          :height (:height page-size)}

                   (dom/text #js {:x (/ (:width page-size) 2)
                                  :y 20
                                  :fontSize "18"
                                  :textAnchor "middle"}
                             (:title (:page state)))

                   (om/build-all render-journalEntry (:mergedJournal state) {:fn #(assoc %
                                                                                    :y (calc-y-pos % (:neighborhood state))
                                                                                    :x (calc-x-pos % (:mergedJournal state))
                                                                                    :color (calc-color % (:neighborhood state)))})

                   ))))


;; below here is a scratch pad for testing things...


