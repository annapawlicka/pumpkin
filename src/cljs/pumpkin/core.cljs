(ns pumpkin.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [om-bootstrap.panel :as p]
            [om-bootstrap.button :as b]
            [om-bootstrap.random :as r]
            [om-bootstrap.nav :as n]
            [om.core :as om :include-macros true]
            [taoensso.sente :as s]
            [cljs.core.async :as async :refer [<! >! chan mult tap sliding-buffer untap close!]]
            [sablono.core :as html :refer-macros [html]]
            [pumpkin.widgets.numbers :as numbers]
            [pumpkin.widgets.charts :as charts]
            [pumpkin.dev.data :as dev]
            [pumpkin.common :refer [log] :as common]
            [cljs-time.format :as tf]
            [cljs-time.coerce :as tc]))

(enable-console-print!)

(defn parse-frequencies
  "Turn unix timestamps into instances of Date and get absolute
  values of additions and deletions (deletions are negative numbers)."
  [data]
  (mapv #(assoc % :timestamp (common/get-date-from-week (:week %))
               :value (js/Math.abs (:value %)))
        data))

(defonce app-state
  (atom {:state :unknown
         :menu {:location :hecuba}
         :rota {}
         :repositories {:view {:current :charts}
                        :github {:issues {:title "Open Issues"
                                          :value "N/A"
                                          :refresh-rate 21600000 ;; 6 hours
                                          :updated-at nil}
                                 :pulls {:title "Open Pull Requests"
                                         :value "N/A"
                                         :refresh-rate 21600000 ;; 6 hours
                                         :updated-at nil}
                                 :code-frequency {:title "Code Frequency"
                                                  :data (parse-frequencies dev/github-frequencies)
                                                  :refresh-rate 21600000
                                                  :updated-at nil}
                                 :contributors {:title "Contributors"
                                                :data  dev/contributors
                                                :refresh-rate 21600000
                                                :updated-at nil}}}
         :clock {:time (numbers/get-time nil)}}))

(defn view-toggle []
  (om/ref-cursor (-> (om/root-cursor app-state) :repositories :view)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; WebSocket events                                                                      ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(let [{:keys [chsk ch-recv send-fn]}
      (s/make-channel-socket! "/ws" {} {:type :auto})]
  (def chsk       chsk)
  (def ch-chsk    ch-recv)
  (def chsk-send! send-fn))

(defn send-message [msg] (chsk-send! msg))

(defmulti handle-event (fn [[id msg] app owner] id))

(defmethod handle-event :session/state [[_ state] app owner]
  (om/update! app :state state))

(defmethod handle-event :dashboard/github-issues [[_ msg] app owner]
  (om/update! app [:repositories :github :issues :updated-at] (new js/Date))
  (om/update! app [:repositories :github :issues :value] (count msg)))

(defmethod handle-event :dashboard/github-pulls [[_ msg] app owner]
  (om/update! app [:repositories :github :pulls :updated-at] (new js/Date))
  (om/update! app [:repositories :github :pulls :value] (count msg)))

(defmethod handle-event :dashboard/github-code-frequency [[_ msg] app owner]
  (om/update! app [:github :code-freuqency :updated-at] (new js/Date))
  (om/update! app [:repositories :github :code-frequency :data] (->  msg
                                                                     parse-frequencies)))

(defmethod handle-event :dashboard/github-contributors [[_ msg] app owner]
  (om/update! app [:github :contributors :updated-at] (new js/Date))
  (om/update! app [:repositories :github :contributors :data] (->  msg
                                                                   js->clj)))

(defmethod handle-event :default [[id msg] app owner])

(defn test-session [owner]
  (chsk-send! [:session/status]))

(defn event-loop [app owner]
  (go (loop [{:keys [event]} (<! ch-chsk)]
        (let [[e payload] event]
          (when-let [[id msg] (seq payload)]
            (case e
              :chsk/recv (handle-event payload app owner)
              (test-session owner))))
        (recur (<! ch-chsk)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Navigation                                                                            ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn nav-bar [cursor owner]
  (reify
    om/IRender
    (render [_]
      (log "Rendering: nav-bar")
      (let [location (:location cursor)]
        (n/navbar
         {:class-name "navbar"}
         (n/nav
          {:collapsible? true}
          (n/nav-item {:key :rota :on-select (fn [e] (om/update! cursor :location e))} "Rota")
          (n/nav-item {:key :hecuba :on-select (fn [e] (om/update! cursor :location e))} "Hecuba"))
         (html [:p.pull-right.pad-top "Showing: " (name location)]))))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Views                                                                                 ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti parse-contributors (fn [data view] (log "parsing contributors for view: " view) view))
(defmethod parse-contributors :table [data _]
  (let [d (mapv (fn [user]
                     (assoc user :weeks (mapv #(assoc % :w (common/get-date-from-week (:w %))) (:weeks user))))
                   data)]
    d))

(defmethod parse-contributors :charts [data _]
  (let [weeks (map :w (:weeks (first data)))
        d  (mapv (fn [week]
                      (hash-map :week (common/get-date-from-week week)
                                :data (mapv (fn [d]
                                              (let [weekly-stat (-> (filter #(= (:w %) week) (:weeks d)) first)]
                                                (hash-map :username (-> d :author :login)
                                                          :deletions (-> weekly-stat :d)
                                                          :additions (-> weekly-stat :a)
                                                          :commits (-> weekly-stat :c)))) data))) weeks)]
    d))

(defn team-members-stats-view [cursor owner {:keys [mult-chan]}]
  (om/component
   (log "Rendering: team-members-stats-view")
   (html
    [:div.table-view
     (for [member (parse-contributors cursor :table)]
       (let [c (chan (sliding-buffer 100))]
         [:div.col-sm-4.col-centered
          (om/build numbers/team-member member {:key :author :init-state {:chan (tap mult-chan c)}})]))])))

(defn chart-stats [cursor owner {:keys [y-axis color]}]
  (reify
    om/IInitState
    (init-state [_]
      {:value {}
       :click false})
    om/IWillMount
    (will-mount [_]
      (go-loop []
        (let [event-chan         (om/get-state owner :chan)
              {:keys [event v]}  (<! event-chan)
              bisect             (-> js/d3 (.bisector (fn [d] (aget d "week"))) .-right)]
          (cond
           (= event :click) (let [data   (-> cursor clj->js)
                                  index  (bisect data v 1)
                                  value  (js->clj (aget data index))]
                                  (om/set-state! owner :value value)
                                  (om/set-state! owner :click true))))
        (recur)))
    om/IRenderState
    (render-state [_ state]
      (log "Rendering: chart-stats")
      (html
       [:div
        (om/build charts/bar-chart (if (om/get-state owner :click) (:value state) [])
                  {:opts {:chart {:div {:id (str "chart-" y-axis) :width "100%" :height 300}
                                  :bounds {:x "10%" :y "5%" :width "80%" :height "75%"}
                                  :x-axis "username"
                                  :y-axis y-axis
                                  :plot js/dimple.plot.bar
                                  :color color}}})]))))

(defn charts-stats-view [cursor owner {:keys [mult-chan]}]
  (om/component
   (log "Rendering: charts-stats-view")
   (html
    [:div.chart
     (let [data (parse-contributors cursor :charts)]
       (for [item [{:k :deletions :color "#d62728"} {:k :additions :color "#4575b4"}]]
         (let [c (chan (sliding-buffer 100))]
           [:div.col-centered
            (om/build chart-stats data {:key (:k item) :init-state {:chan (tap mult-chan c)}
                                        :opts {:y-axis (name (:k item))
                                               :color (:color item)}})])))])))

(defn table-stats-view [cursor owner {:keys [mult-chan]}]
  (om/component
   (log "Rendering: table-stats-view")
   (html
    [:div.chart
     (let [data (parse-contributors cursor :charts)]
       (for [item [{:k :deletions :color "#d62728"} {:k :additions :color "#4575b4"}]]
         (let [c (chan (sliding-buffer 100))]
           [:div.col-centered
            [:table.table
             ]
            ])))])))

(defmulti code-frequency-stats (fn [cursor owner opts]
                                 (let [{:keys [current-view data]} cursor]
                                   (log "rendering: code-frequency-stats for " current-view)
                                   current-view)))
(defmethod code-frequency-stats :charts [{:keys [data]} owner {:keys [mult-chan]}]
  (om/component
   (html
    [:div.chart
     (om/build charts-stats-view data {:opts {:mult-chan mult-chan}})])))

(defmethod code-frequency-stats :table [{:keys [data]} owner {:keys [mult-chan]}]
  (om/component
   (html
    [:div
     (om/build table-stats-view data {:opts {:mult-chan mult-chan}})])))

(defmethod code-frequency-stats :cards [{:keys [data]} owner {:keys [mult-chan]}]
  (om/component
   (html
    [:div
     (om/build team-members-stats-view data {:opts {:mult-chan mult-chan}})])))

(defn send-messages [urls]
  (doseq [{:keys [k url]} urls]
    (send-message [k {:url url}])))

(defn toggles [cursor owner]
  (om/component
   (log "Rendering: toggles")
   (let [toggle  (om/observe owner (view-toggle))
         current (:current toggle)]
     (html
      (b/toolbar {}
       (b/button {:bs-size "xsmall" :class (str "pull-right " (if (= current :table) "active" ""))
                  :key :table
                  :on-click (fn [e]
                              (om/update! toggle :current :table))}
                 "Table")
       (b/button {:bs-size "xsmall" :class (str "pull-right " (if (= current :cards) "active" ""))
                  :key :cards
                  :on-click (fn [e]
                              (om/update! toggle :current :cards))}
                 "Cards")
       (b/button {:bs-size "xsmall" :class (str "pull-right " (if (= current :charts) "active" ""))
                  :key :charts
                  :on-click (fn [e]
                              (om/update! toggle :current :charts))}
                 "Charts"))))))

(defn repository-view [cursor owner {:keys [url]}]
  (reify
    om/IInitState
    (init-state [_]
      (let [hover-chan (chan (sliding-buffer 100))
            m          (mult hover-chan)]
        {:hover-chan hover-chan
         :mult-chan m}))
    om/IDidMount
    (did-mount [_]
      (send-messages [{:k :dashboard/github-issues          :url (str url "/issues?state=open") }
                      {:k :dashboard/github-pulls           :url (str url "/pulls?state=open")}
                      {:k :dashboard/github-code-frequency  :url (str url "/stats/code_frequency")}
                      {:k :dashboard/github-contributors    :url (str url "/stats/contributors")}]))
    om/IRender
    (render [_]
      (log "Rendering: repository-view")
      (html
       [:div
        (let [{:keys [hover-chan mult-chan]} (om/get-state owner)
              toggle (om/observe owner (view-toggle))
              current-view (:current toggle)]
          [:div.col-md-12.view
           [:div.col-md-6 ;; LEFT
            [:div.row
             [:div.pad-top
              (p/panel {:header "Additions and deletions per week"}
                       (om/build charts/stacked-bar-chart (:code-frequency cursor)
                                 {:opts {:hover-chan hover-chan}}))]]
            [:div.row.row-centered
              [:div.col-sm-4.col-centered
               (om/build numbers/changing-color-number (:issues cursor)
                         {:opts {:scale {:bad-value 100 :good-value 0}}})]
              [:div.col-sm-4.col-centered
               (om/build numbers/changing-color-number (:pulls cursor)
                         {:opts {:scale {:bad-value 100 :good-value 0}}})]]]
           [:div.col-md-6 ;; RIGHT
            [:div.row.row-centered.pad-top
             (p/panel {:header (om/build toggles nil)}
                      (om/build code-frequency-stats {:data (-> cursor :contributors :data)
                                                      :current-view current-view}
                        {:opts {:mult-chan mult-chan}}))]]])]))))

(defn rota-view [cursor owner]
  (reify
    om/IRender
    (render [_]
      (log "Rendering: rota-view")
      (html
       [:div "Rota"]))))

(defmulti current-view (fn [app owner] (-> app :menu :location)))

(defmethod current-view :rota [app owner]
  (om/component (html [:div (om/build rota-view (-> app :rota))])))

(defmethod current-view :hecuba [app owner]
  (om/component (html [:div (om/build repository-view (-> app :repositories :github)
                                      {:opts {:url "https://api.github.com/repos/mastodonc/kixi.hecuba"}})])))

;; TODO
;; 1. legend to chart (red deletions, blue additions)
;; 3. add a component with other stats, e.g. c4, kaylee

(defn dashboard [app owner]
  (reify
    om/IRender
    (render [_]
      (html
       [:div
        [:div.container-fluid.dashboard
         ;; Jumbotron
         (r/jumbotron {} (html [:h1 "Team Dashboard" [:small.pull-right (om/build numbers/clock (:clock app))]]))
         ;; Navbar
         (om/build nav-bar (:menu app))
         ;; Current View
         (om/build current-view app)]]))))

(defn application [cursor owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (event-loop cursor owner))
    om/IRender
    (render [_]
      (html
       [:div
        (case (:state cursor)
          :open (om/build dashboard cursor)
          :unknown [:div "Loading dashboard..."])]))))

(defn main []
  (om/root
    application
    app-state
    {:target (. js/document (getElementById "app"))}))
