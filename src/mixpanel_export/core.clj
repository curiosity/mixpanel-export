(ns mixpanel-export.core
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [clj-time.format :as tf]
            [clojure.string :as str]
            [pandect.algo.md5 :refer [md5]]))

(def ^:dynamic *base-uri* "https://data.mixpanel.com")
(def ^:dynamic *api-key* (System/getenv "MIXPANEL_API_KEY"))
(def ^:dynamic *api-secret* (System/getenv "MIXPANEL_API_SECRET"))
(def ^:dynamic *expiration-seconds* 120)

(def uri-time-formatter (tf/formatter "YYYY-MM-dd"))

(defn unix-time
  ([]
   (unix-time (t/now)))
  ([dt]
   (quot (tc/to-long dt) 1000)))

(defn format-param
  [p]
  (if (vector? p)
    (json/generate-string p)
    (str p)))

(defn calc-signature
  [api-secret params]
  (let [params-concat (->> (sort-by first params)
                           (map #(str/join "=" [(first %) (format-param (second %))]))
                           (str/join ""))]
    (md5 (str params-concat api-secret))))

(defn export
  " Exports all mixpanel events specified by events from from to to
   ---
   Events is a vector of mixpanel event names as string we want to return.
   From is a DateTime and inclusive
   To is a DateTime and inclusive"
  [events from to]
  (let [query-params {"expire" (+ (unix-time) *expiration-seconds*)
                      "from_date" (tf/unparse uri-time-formatter from)
                      "to_date" (tf/unparse uri-time-formatter to)
                      "event" (json/generate-string events)
                      "api_key" *api-key*}
        sig (calc-signature *api-secret* query-params)
        results (http/get (str *base-uri* "/api/2.0/export/")
                          {:basic-auth [*api-key* *api-secret*]
                           :query-params (assoc query-params "sig" sig)})]
      (some->> results
               :body
               str/split-lines
               (map json/parse-string))))

(def export-registered-events
  (partial export ["User Register"]))

(defn spit-json
  [f obj]
  (spit f (json/generate-string obj)))

(comment
  (def now (t/now))
  (def last-month (t/minus now (t/months 1)))
  (spit-json "output.json" (export-registered-events last-month now)))
