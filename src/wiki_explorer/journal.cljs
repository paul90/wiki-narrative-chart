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
            [cljs.core.async :refer [<!]]
            [clojure.walk :as walk]))


;; ### Fetch Page from server

(def ^:dynamic page-journal)

(defn set-journal
  [journal]
  (def page-journal journal))

(defn get-journal
  [url]
  (def page-journal [])
  (go
   (let [body (<! (http/get url {:with-credentials? false}))]
     (set-journal (:journal (:body body))))
  ; need to pause here for the response to arrive
   )
  page-journal)

;; ### Extracting a page's journal data
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

(defn journal-extract
  "Given the URL for a page, we extract the journal"
  [url]
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
                                    (get-journal url)))))

(comment
(defn journal-extract
  "Given the URL for a page, we extract the journal"
  [url]
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
                                    (:journal
                                     (json/read (:body (http/get url
                                                                 {:accept :json}))
                                                    :key-fn keywork))))))
)


;; below here is some trials, to get the code right...



(prn (journal-extract "http://localhost:3000/chorus-of-voices.json"))


(journal-extract "http://ward.fed.wiki.org/chorus-of-voices.json")
