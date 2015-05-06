(ns verifydata-id.core
  (:require
   [cats.core :as m]
   [cats.monad.maybe :as maybe]
   [net.cgrand.enlive-html :as html]
   )
  (:use [liberator.core :only [defresource resource]]
        [ring.middleware.params :refer [wrap-params]]
        [compojure.core :refer [defroutes ANY]]
        [hiccup.page :only [html5]]))

(def ^:dynamic *base-url* "https://data.kpu.go.id/ss8.php?cmd=cari&nik=")

;; Forward declarations
(def process-kpu-response)

(defn fetch-url [url]
  (try
    (maybe/just (html/html-resource (java.net.URL. url)))
    (catch Exception e (maybe/nothing))))

(defn fetch-kpu-response [nik]
  (m/mlet [data (fetch-url (str *base-url* nik))]
    (m/return (process-kpu-response data))))

; http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Clojure
(defn levenshtein [str1 str2]
  "a Clojure levenshtein implementation using transient data structure"
  (let [n (count str1) m (count str2)]
    (cond 
     (= 0 n) m
     (= 0 m) n
     :else
     (let [prev-col (transient (vec (range (inc m)))) col (transient [])] ; initialization for the first column.
       (dotimes [i n]
         (assoc! col 0 (inc i)) ; update col[0]
         (dotimes [j m]
           (assoc! col (inc j)  ; update col[1..m] 
                   (min (inc (get col j))
                        (inc (get prev-col (inc j)))
                        (+ (get prev-col j) (if (= (get str1 i) (get str2 j)) 0 1)))))
         (dotimes [i (count prev-col)] 
           (assoc! prev-col i (get col i)))) ; 
       (last (persistent! col)))))) ; last element of last column

(defn process-kpu-response [body]
  (let [data
        (map html/text
             (html/select body [:form :div.form :span]))]
    (into {} (map vec (partition 2 data)))))

(defresource home
  :available-media-types ["text/html"]
  :handle-ok
  (fn [ctx]
    (let []
      (html5 {}
             [:head
              [:title "Indonesian Data Verification Service"]]
             [:body
              "Visit /verify-nik/:nik or /verify-nik-name/:nik/:name"])
      )))

(defresource verify-nik [nik]
  :available-media-types ["application/json"]
  :handle-ok
  (fn [_]
    (let [mdata
          (fetch-kpu-response nik)
          ]
      (cond
        (= (type mdata) cats.monad.maybe.Nothing)
        {:state 'error}
        :else
        (let [data (maybe/from-maybe mdata)
              name (data "Nama:")]
          (if name
            {:state 'found
             :name name}
            {:state 'not-found}))))))

(defresource verify-nik-name [nik, name]
  :available-media-types ["application/json"]
  :handle-ok
  (fn [_]
    (let [mdata
          (fetch-kpu-response nik)
          ]
      (cond
        (= (type mdata) cats.monad.maybe.Nothing)
        {:state 'error}
        :else
        (let [data (maybe/from-maybe mdata)
              reg-name (data "Nama:")]
          (if name
            {:state 'found
             :name name
             :reg-name reg-name
             :distance (levenshtein name reg-name)}
            {:state 'not-found}))))))

(defroutes app
  (ANY "/" [] home)
  (ANY "/verify-nik/:nik" [nik] (verify-nik nik))
  (ANY "/verify-nik-name/:nik/:name" [nik, name] (verify-nik-name nik name))
  )

(def handler
  (-> app
      wrap-params))
