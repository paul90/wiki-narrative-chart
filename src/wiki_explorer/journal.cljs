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
            [wiki-explorer.data :as data]
            [cuerdas.core :as str]))

;; Processing the journal data is broken up into a number of steps.
;; Starting with fetching the page from the server.

;; ### Fetch Page from server
;;
;; As this is performed asynchronously we will use `page-journal`
;; to hold the journal data, so we can wait before running the
;; next step.
;;
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


;; ### Rename data keys for fork events
;;
;; A fork journal entry marks the forking of the page from remote site. The `:site`
;; gives the name of the remote site. We rename this entry to `:fork-from` as it is
;; a better name, and we will be adding a `:site` to every journal entry indication
;; which site the entry was made one.

(defn rename-site-key
  [journalData]

  (walk/postwalk-replace {:site :fork-from} journalData))




;; ### Add any missing dates
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

(defn add-missing-dates
  "We check that each entry has a date, if it is missing we will add
  one, making it 10ms later than the previous journal entry.
  Given a page journal, we some inital checks to ensure that each
  entry has a date. We also rename `:site` to `:fork-from`, in
  preparation for adding a `:site` to each entry in step-?"
  [journalData]

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
             journalData)))

;; ### Add site to journal entry
;;
;; Journal entries don't contain details about which site the entry was made on.
;; However, we can infer this detail by walking the journal in reverse as we know
;; which site the journal has been retrieved from. Any fork events we find change
;; the site the entry was made on.

(def ^:dynamic curr-site "")

(defn set-curr-site
  [site]
  "used to change the current site when we encounter a fork"
  (def curr-site site))

(def ^:dynamic next-site "")

(defn set-next-site
  [site]
  "used to change the current site when we encounter a fork"
  (def next-site site))

(defn add-journal-entry-site
  [state currentSite journalData]

  (do
    (set-next-site currentSite)
    (loop [journalData journalData, acc ()]
      (if (empty? journalData)
        (into [] acc)
        (recur (drop-last journalData)
               (do
                 (set-curr-site next-site)
                 (if (= (:type (last journalData)) "fork")
                   (do
                     ;;
                     ;; need to add check from missing fork-from
                     (if-not (nil? (:fork-from (last journalData)))
                       ;; fork-from not null
                       (do
                         (set-next-site (:fork-from (last journalData)))
                         (data/add-neighbor state next-site))
                       ;; fork-from is null - check for already forked from local
                       (do
                         (if-not (str/endswith? next-site ":local")
                           (set-next-site (str next-site ":local")))))))
                 (conj acc (assoc (last journalData) :site curr-site))))))))


;; ### Merge the current journal into the neighborhood journal

(defn merge-into-neighborhood
  ""
  [state journalData]

  (swap! state assoc-in [:mergedJournal] (sort-by :date (distinct (into (:mergedJournal @state) journalData)))))





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
           ;; we change the state of the neighbor we are process to indicate we have started processing
           ;; the journal data - the site icon will spin faster...
           (data/change-neighbor-state state currentSite "process")
           (->> page-journal
                (rename-site-key)
                (add-missing-dates)
                (add-journal-entry-site state currentSite)
                (merge-into-neighborhood state))
           (prn "done -> " currentSite)
           (data/change-neighbor-state state currentSite "done"))
         (do
           ;; fetching the page failed, the state of the neighbor is changed to `fail`.
           (prn "fetch failed" currentSite)
           (data/change-neighbor-state state currentSite "fail")))
       (swap! state assoc-in [:processQueue] (pop (:processQueue @state)))
       (recur)))))







;; below here is some trials, to get the code right...



