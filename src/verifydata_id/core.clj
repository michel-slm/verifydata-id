(ns verifydata-id.core
  (:require 
   [net.cgrand.enlive-html :as html]
   )
  (:use [liberator.core :only [defresource resource]]
        [ring.middleware.params :refer [wrap-params]]
        [compojure.core :refer [defroutes ANY]]
        [hiccup.page :only [html5]]))

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

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
    (let [data
          (fetch-url
           (str "https://data.kpu.go.id/ss8.php?cmd=cari&nik=" nik))
          ]
      (process-kpu-response data))))
         

(defroutes app
  (ANY "/" [] home)
  (ANY "/verify-nik/:nik" [nik] (verify-nik nik)))

(def handler
  (-> app
      wrap-params))
