(ns pumpkin.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [taoensso.sente :as s]
            [cljs.core.async :as async :refer [<! >! chan]]
            [sablono.core :as html :refer-macros [html]]
            [pumpkin.widgets.numbers :as numbers]
            [pumpkin.widgets.charts :as charts]))

(enable-console-print!)

(defonce app-state
  (atom {:state :unknown
         :github {:issues {:title "GitHub Issues"
                           :value "N/A"
                           :refresh-rate 21600000 ;; 6 hours
                           :updated-at "N/A"}
                  :code-frequency {:title "Code Frequency"
                                   :data []
                                   :refresh-rate 21600000
                                   :updated-at "N/A"}}}))

(let [{:keys [chsk ch-recv send-fn]}
      (s/make-channel-socket! "/ws" {} {:type :auto})]
  (def chsk       chsk)
  (def ch-chsk    ch-recv)
  (def chsk-send! send-fn))

(defn send-message [msg] (chsk-send! msg))

(defmulti handle-event (fn [[id msg] app owner] (println (str "Received e: " id)) id))

(defmethod handle-event :session/state [[_ state] app owner]
  (om/update! app :state state))

(defmethod handle-event :test/reply [[_ msg] app owner]
  (om/update! app :data/text msg))

(defmethod handle-event :test/github-issues [[_ msg] app owner]
  (om/update! app [:github :issues :updated-at] (js/Date))
  (om/update! app [:github :issues :value] (count msg)))

(defmethod handle-event :test/github-code-frequency [[_ msg] app owner]
  (om/update! app [:github :code-freuqency :updated-at] (js/Date))
  (om/update! app [:github :code-frequency :data] msg))

(defmethod handle-event :default [[id msg] app owner])

(defn test-session [owner]
  (println "Checking session status")
  (chsk-send! [:session/status]))

(defn event-loop [app owner]
  (go (loop [{:keys [event]} (<! ch-chsk)]
        (let [[e payload] event]
          (when-let [[id msg] (seq payload)]
            (case e
              :chsk/recv (handle-event payload app owner)
              (test-session owner))))
        (recur (<! ch-chsk)))))

(defn dashboard [app owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (send-message [:test/github-issues
                     {:url "https://api.github.com/repos/mastodonc/kixi.hecuba/issues?state=open"}])
      (send-message [:test/github-code-frequency
                     {:url "https://api.github.com/repos/mastodonc/kixi.hecuba/stats/code_frequency"}]))
    om/IRender
    (render [_]
      (html
       (let [{:keys [github]} app]
         [:div
          ;; Numbers
          [:div
           (om/build numbers/simple-number (:issues github)
                     {:opts {:color "green"}})]
          ;; Charts
          [:div.col-md-6
           (om/build charts/chart-figure (:code-frequency github)
                     {:opts {:chart {:div {:id "chart" :width "100%" :height 500}
                                     :bounds {:x "5%" :y "15%"
                                              :width "80%" :height "50%"}
                                     :x-axis "week"
                                     :y-axis "additions"
                                     :plot js/dimple.plot.bar}}})
           ]])))))

(defn application [cursor owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (event-loop cursor owner))
    om/IRender
    (render [_]
      (html
       [:div.col-md-12
        (case (:state cursor)
          :open (om/build dashboard cursor)
          :unknown [:div "Loading dashboard..."])]))))

(defn main []
  (om/root
    application
    app-state
    {:target (. js/document (getElementById "app"))}))
