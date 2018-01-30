(ns socket-task.core
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [cljs.core.async :as a :refer [<! >!]]
              [haslett.client :as ws]
              [haslett.format :as fmt]
              [cljs.tools.reader :refer [read-string]]
              [cljs.js :refer [empty-state eval js-eval]]))

(enable-console-print!)

(println "This text is printed from src/socket-task/core.cljs. Go ahead and edit it and see reloading in action.")

(js/console.log "Sample text!")

(def operations (into #{} (clojure.string/split "+-/*" #"")))
(def operation->func {"+" + "-" - "/" / "*" *})

(defn calculate [string]
  (let [tokens-strings (-> string
                           (clojure.string/split #" "))]
    (reduce (fn [res token]
              (if (contains? operations token)
                (let [op2 (first res)
                      op1 (first (drop 1 res))
                      operand token
                      operation-result (str ((operation->func operand) (js/parseInt op1) (js/parseInt op2)))]
                  (conj (drop 2 res) operation-result))
                (conj res token)))
            
      ()
      tokens-strings)))

(go (let [stream (<! (ws/connect "ws://rpn.javascript.ninja:8080"))]
      (dotimes [i 61]
        (let [string-in-polish (<! (:source stream))
              result (first (calculate string-in-polish))]
          (js/console.log (str "iteration #" i))
          (js/console.log string-in-polish)
          (js/console.log result)
          (js/console.log (js/parseFloat result))
          (>! (:sink stream) (js/parseInt result))))
      (ws/close stream)))

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))


(defn on-js-reload [])
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)

; token is eyJhbGciOiJIUzI1NiJ9.MTUxNzM1NDQzMzQ1Ng.Xhs_oz1yXFJW58wTYF11fq5LR-wZZoXuH3yQKWMU4l0
; yet another token is eyJhbGciOiJIUzI1NiJ9.MTUxNzM1NDUyODIyNg.d92VBxbr3hOTYCzM27UyBmY-BH29UbYeGx57hsSQ93w

