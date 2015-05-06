(ns verifydata-id.core
  (:use [liberator.core :only [defresource resource]]
        [ring.middleware.params :refer [wrap-params]]
        [compojure.core :refer [defroutes ANY]]))

(defresource verify-nik [nik]
  :available-media-types ["text/plain"]
  :handle-ok (fn [_] (format "The NIK is %s" nik)))
  
(defroutes app
  (ANY "/verify-nik/:nik" [nik] (verify-nik nik)))

(def handler
  (-> app
      wrap-params))
