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
              "Visit /verify-nik/NIK"])
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

(defroutes app
  (ANY "/" [] home)
  (ANY "/verify-nik/:nik" [nik] (verify-nik nik)))

(def handler
  (-> app
      wrap-params))
