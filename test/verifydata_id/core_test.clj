(ns verifydata-id.core-test
  (:require [clojure.test :refer :all]
            [cats.monad.maybe :as maybe]
            [verifydata-id.core :refer :all]))

(deftest kpu-down-test
  (testing "Test lookup against downed KPU server"
    (with-redefs [fetch-url (fn [url] (maybe/nothing))]
      (is (= (verify-nik-fn 42) {:state 'error})))))
