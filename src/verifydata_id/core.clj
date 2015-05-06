(ns verifydata-id.core
  (:require [org.httpkit.client :as http])
  (:use [liberator.core :only [defresource resource]]
        [ring.middleware.params :refer [wrap-params]]
        [compojure.core :refer [defroutes ANY]]))

(defresource verify-nik [nik]
  :available-media-types ["text/html"]
  :handle-ok
  #(let [media-type
         (get-in % [:representation :media-type])
         {:keys [status headers body error] :as resp}
         @(http/get
           (str "https://data.kpu.go.id/ss8.php?cmd=cari&nik=" nik))
         ]
     (if error
       (format "Failed, exception: %s" error)
       body)))

(defroutes app
  (ANY "/verify-nik/:nik" [nik] (verify-nik nik)))

(def handler
  (-> app
      wrap-params))
