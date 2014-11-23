;; ## Merging Page Journals
;;
;; Taking a page's journals from each of its twins within a
;; neighborhood it is possible to merge them to construct a
;; view of how the page has not only changed over time, but
;; also how it has changed across the neighborhood.
;;
(ns wiki-explorer.journal
  "Contains functions for manipulation of page journal data"
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :as async :refer [<! >! chan close!]]
            [clojure.walk :as walk]
            [goog.net.XhrIo :as xhr]
            [wiki-explorer.data :as data]))


;; ### 0. Fetch Page from server
;;
;; As this is performed asynchronously we will use `page-journal`
;; to hold the journal data, so we can wait before running the
;; next step.
;;
;; TO-DO: error detection
;;

(def ^:dynamic done "waiting")

(def ^:dynamic page-journal [])

(defn set-journal
  [journal]
  (def page-journal journal))

(defn get-journal
  [url]
  (def page-journal [])
  (def done "waiting")
  (go
   (let [response (<! (http/get url {:with-credentials? false}))]
     (def done (:success response))
     (if done
       (set-journal (:journal (:body response)))))))

;; ### 1. Extracting a page's journal data
;;
;; Not all journal entries have a date, so while we are extracting
;; the journal, we will also detect those journal entries that are
;; without one, and give them one. As these entries mainly appear
;; to be *forks*, it should be safe to add a few milliseconds to the
;; previous entry, and still preserve the overall order.
;;

(def ^:dynamic je-date 0)

(defn set-je-date
  [date]
  (def je-date date))

(defn step-1
  "Given a page journal, we some inital checks to ensure that each
  entry has a date. We also rename `:site` to `:fork-from`, in
  preparation for adding a `:site` to each entry in step-?"
  []




   (set-journal
    (walk/postwalk-replace {:site :fork-from}
                           (into []
                                 (map (fn [je]
                                        (into (sorted-map)
                                              (do
                                                (set-je-date (+ je-date 10))
                                                (if (:date je)
                                                  (do
                                                    (set-je-date (:date je))
                                                    (assoc je :date je-date))
                                                  (assoc je :date je-date)))))
                                      page-journal))))
  )

;; ### 2.
;;





;; ### Build Journal for Page in Neighborhood
;;
;;

(def ^:dynamic currentSite "")


(defn build-neighborhood-journal
  "We process the process queue, adding to the neighborhood journal
  for the instance of the page on each of the sites in the queue"
  [state]
;; we loop over the `:processQueue` until it is empty
  (go
   (loop []
     (when (not (empty? (peek (:processQueue @state))))
       (def currentSite (peek (:processQueue @state)))
;; change the state of the current site, in the neighborhood to *fetch* to
;; provide visual feedback that it is being processed
       (data/change-neighbor-state state currentSite "fetch")
       (prn "fetching -> " currentSite)
;; we get the page json, and extract the journal data.
       (get-journal (clojure.string/join ["http://" currentSite "/" (:slug (:page @state)) ".json"]))
;; having initiated the data fetch, we need to wait for it to complete

       (loop []
         (if (= done "waiting")
           (do
;; if we are still *waiting* we will wait for 2000ms and look again
             (prn "waiting...")
             (<! (async/timeout 2000))
             (recur))
           (prn "done waiting...")))
;; the fetch has now completed, `done` will be *true* if the page was fetched sucessfully, and
;; be *false* if it failed.
       (if done
         (do
           (-> page-journal
               ())
           (prn "done -> " currentSite)
           (data/change-neighbor-state state currentSite "done"))
         (do
           (prn "fetch failed" currentSite)
           (data/change-neighbor-state state currentSite "fail")))
       (swap! state assoc-in [:processQueue] (pop (:processQueue @state)))
       (recur)))))







;; below here is some trials, to get the code right...


