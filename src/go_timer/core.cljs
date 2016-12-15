(ns go-timer.core
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

(defonce app-state
  (atom {:left-timer  {:secs 1800 :on? false}
         :right-timer {:secs 1800 :on? false}
         :interval    nil}))

(defn clock [secs]
  (let [hours    (Math/floor (/ secs 3600))
        mins     (mod (Math/floor (/ secs 60)) 60)
        secs     (mod secs 60)
        add-zero #(if (< % 10) (str "0" %) %)]
    [:div.clock
     (str (add-zero hours) ":" (add-zero mins) ":" (add-zero secs))]))

(defn switch [on? on-toggle]
  [:input.switch {:type         "radio"
                  :checked      on?
                  :on-click     on-toggle
                  :on-key-press on-toggle}])

(defn start-timer [timer-id]
  (let [interval (js/setInterval (fn []
                                   (swap! app-state
                                          #(update-in % [timer-id :secs] dec)))
                                 1000)]
    (swap! app-state #(-> %
                          (assoc-in [timer-id :on?] true)
                          (assoc :interval interval)))))

(defn stop-timer [timer-id]
  (js/clearInterval (@app-state :interval))
  (swap! app-state #(assoc-in % [timer-id :on?] false)))

(defn toggle-timers [timer-id]
  (let [states [(-> @app-state :left-timer :on?)
                (-> @app-state :right-timer :on?)]]
    (condp = states
      [false false] (start-timer timer-id)
      [true false]  (do (stop-timer :left-timer)
                        (start-timer :right-timer))
      [false true]  (do (stop-timer :right-timer)
                        (start-timer :left-timer)))))

(defn timer [timer-id]
  [:div.timer
   [clock (-> @app-state timer-id :secs)]
   [switch (-> @app-state timer-id :on?) #(toggle-timers timer-id)]])

(defn time-presets []
  [:select.time-presets
   {:on-change (fn [event]
                 (let [secs (-> event .-target .-value)]
                   (swap! app-state #(-> %
                                         (assoc-in [:left-timer :secs] secs)
                                         (assoc-in [:right-timer :secs] secs)))))}
   (map (fn [hour]
          [:option {:key hour :value (* hour 3600)} (str hour "h")])
        (range 0.5 3.25 0.5))])

(defn pause []
  [:button.pause
   {:on-click #(do (stop-timer :left-timer)
                   (stop-timer :right-timer))}
   "pause"])

(defn go-timer []
  [:div.go-timer
   [timer :left-timer]
   [timer :right-timer]
   [:div.controls
    [time-presets]
    [pause]]])

(reagent/render-component [go-timer]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
