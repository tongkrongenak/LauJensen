(ns clojureql.pagination
  (:refer-clojure :exclude [drop take])
  (:use [clojureql.core :only (drop take)]))

(def *per-page* 25)

(defn- assert-page
  "Ensure that page is greater than zero."
  [page] (if-not (pos? page) (throw (IllegalArgumentException. "Page must be greater than 0."))))

(defn- parse-int [number & {:keys [junk-allowed radix]}]
  (if (integer? number)
    number
    (try
      (Integer/parseInt (str number) (or radix 10))
      (catch Exception e
        (when-not junk-allowed
          (throw e))))))

(defn offset
  "Calculate the SQL offset from page and per-page."
  [page per-page]
  (assert-page page)
  (* (dec page) per-page))

(defn total
  "Count the total number of records for the given relation."
  [table & {:keys [page per-page]}]
  (:count (first @(assoc table :tcols [:count/* :as :count] :order-by nil))))

(defn paginate
  "Paginate the given relation by page and per-page."
  [table & {:keys [page per-page]}]
  (let [page (or (parse-int page) 1) per-page (or (parse-int per-page) *per-page*)]
    (with-meta @(-> table (drop (offset page per-page)) (take per-page))
      {:page page
       :per-page per-page
       :total (total table)})))