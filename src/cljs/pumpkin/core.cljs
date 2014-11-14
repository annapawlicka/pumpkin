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
         :repository {:header {:selected-week (common/unparse-date (common/get-date-from-week dev/first-week)
                                                                   "yyyy-MM-dd")}
                      :issues {:title "Open Issues"
                               :value "N/A"
                               :refresh-rate 21600000 ;; 6 hours
                               :updated-at nil}
                      :pulls {:title "Open Pull Requests"
                              :value "N/A"
                              :refresh-rate 21600000 ;; 6 hours
                              :updated-at nil}
                      :code-frequency {:title "Code Frequency"
                                       :div {}
                                       :event-toggle {:current :hover}
                                       :data [] ;; (parse-frequencies dev/github-frequencies)
                                       :refresh-rate 21600000
                                       :updated-at nil}
                      :contributors {:title "Contributors"
                                     :view {:current :charts}
                                     :data [] ;; dev/contributors
                                     :div {:width "100%" :height "100%"}
                                     :refresh-rate 21600000
                                     :updated-at nil}}
         :clock {:time (common/get-time nil)}}))

(defn header []
  (om/ref-cursor (-> (om/root-cursor app-state) :repository :header)))

(defn event-type []
  (om/ref-cursor (-> (om/root-cursor app-state) :repository :code-frequency :event-toggle)))

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
  (om/update! app [:repository :issues :updated-at] (new js/Date))
  (om/update! app [:repository :issues :value] (count msg)))

(defmethod handle-event :dashboard/github-pulls [[_ msg] app owner]
  (om/update! app [:repository :pulls :updated-at] (new js/Date))
  (om/update! app [:repository :pulls :value] (count msg)))

(defmethod handle-event :dashboard/github-code-frequency [[_ msg] app owner]
  (om/update! app [:repository :code-frequency :updated-at] (new js/Date))
  (om/update! app [:repository :code-frequency :data] (-> msg parse-frequencies)))

(defmethod handle-event :dashboard/github-contributors [[_ msg] app owner]
  (om/update! app [:repository :contributors :updated-at] (new js/Date))
  (om/update! app [:repository :contributors :data] (->  msg js->clj))
  (om/update! app [:repository :contributors :selected-week] (-> msg js->clj
                                                                 first :weeks
                                                                 first :w common/get-date-from-week
                                                                 (common/unparse-date "yyyy-MM-dd"))))

(defmethod handle-event :default [[id msg] app owner])

(defn test-session [owner]
  (chsk-send! [:session/status]))

(defn event-loop [app owner]
  (go-loop []
    (let [{:keys [event]} (<! ch-chsk)
          [e payload] event]
      (when-let [[id msg] (seq payload)]
        (case e
          :chsk/recv (handle-event payload app owner)
          (test-session owner)))
      (recur))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Views                                                                                 ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti parse-contributors (fn [view] view))
(defmethod parse-contributors :table [_]
  (fn [data]
    (let [d (mapv (fn [user]
                    (assoc user :weeks (mapv #(assoc % :w (common/get-date-from-week (:w %))) (:weeks user))))
                  data)]
      d)))

(defmethod parse-contributors :charts [_]
  (fn [cursor]
    (let [{:keys [data div]} cursor
          weeks (map :w (:weeks (first data)))
          d  (mapv (fn [week]
                     (hash-map :week (common/get-date-from-week week)
                               :data (mapv (fn [d]
                                             (let [weekly-stat (-> (filter #(= (:w %) week) (:weeks d)) first)]
                                               (hash-map :username (-> d :author :login)
                                                         :deletions (-> weekly-stat :d)
                                                         :additions (-> weekly-stat :a)
                                                         :commits (-> weekly-stat :c)))) data))) weeks)]
      (assoc cursor :data d))))

(defn chart-stats [cursor owner {:keys [y-axis color]}]
  (reify
    om/IInitState
    (init-state [_]
      {:c (chan (sliding-buffer 100))
       :value (-> cursor :data first :week)})
    om/IWillMount
    (will-mount [_]
      (let [c              (om/get-state owner :c)
            mult-chan      (om/get-shared owner :mult-chan)
            week           (header)]
        (tap mult-chan c)
        (go-loop []
          (let [event-chan         (om/get-state owner :c)
                {:keys [event v]}  (<! event-chan)]
            (cond
             (= event :mouseover) (when v
                                    (om/set-state! owner :value v)
                                    (om/update! week :selected-week (common/unparse-date v "yyyy-MM-dd")))
             (= event :click)      ((om/set-state! owner :value v)
                                    (om/update! week :selected-week (common/unparse-date v "yyyy-MM-dd")))))
          (recur))))
    om/IRenderState
    (render-state [_ state]
      (html
       (let [id (str "chart-" y-axis)]
         [:div.chart
          (let [value          (common/timestamp->value (:value state) "week" (:data cursor))
                selected-event (om/observe owner (event-type))]
            (om/build charts/bar-chart {:data value :div (:div cursor)}
                      {:opts {:id id
                              :bounds {:x "15%" :y "5%" :width "80%" :height "75%"}
                              :x-axis "username"
                              :y-axis y-axis
                              :plot js/dimple.plot.bar
                              :color color
                              :event-type (:current selected-event)}}))])))
    om/IWillUnmount
    (will-unmount [_]
      (untap (om/get-shared owner :mult-chan) (om/get-state owner :c)))))

(defn chart-stats-view [cursor owner]
  (om/component
   (html
    [:div.row
     (for [item [{:k :deletions :color "#d62728"} {:k :additions :color "#4575b4"}]]
       [:div.col-centered {:style {:width "50%"}}
        (om/build chart-stats cursor {:key (:k item)
                                      :opts {:y-axis (name (:k item))
                                             :color (:color item)}})])])))

(defn team-member
  "Shows team member's info and stats."
  [cursor owner opts]
  (reify
    om/IInitState
    (init-state [_]
      {:c     (chan (sliding-buffer 100))
       :value (-> cursor :weeks first :w)})
    om/IWillMount
    (will-mount [_]
      (let [mult-chan (om/get-shared owner :mult-chan)
            c         (om/get-state owner :c)
            week      (header)]
        (tap mult-chan c)
        (go-loop []
          (let [event-chan         (om/get-state owner :c)
                {:keys [event v]}  (<! event-chan)]
            (when v
              (om/set-state! owner :value v)
              (om/update! week :selected-week (common/unparse-date v "yyyy-MM-dd"))))
          (recur))))
    om/IRenderState
    (render-state [_ state]
      (let [value (common/timestamp->value (:value state) "w" (:weeks cursor))
            {:keys [author]} cursor]
        (html
         [:div.col-sm-4 {:style {:font-size "80%"}}
          [:div.panel.panel-default
           [:div.panel-heading
            [:h4.panel-title [:p (:login author)] [:p (when-let [uri (:avatar_url author)]
                                [:img.img-thumbnail.table-image.pull-right
                                 {:src uri :style {:max-width "25%"}}])]]]
           [:div.panel-body {:style {:height 80}}
            [:div
             [:p "Additions: " (aget value "a")]
             [:p "Deletions: " (aget value "d")]
             [:p "Total commits: " (aget value "c")]]]]])))
    om/IWillUnmount
    (will-unmount [_]
      (untap (om/get-shared owner :mult-chan) (om/get-state owner :c)))))

(defn team-members-stats-view [cursor owner]
  (om/component
   (html
    [:div.row
     [:div.table-view
      (om/build-all team-member cursor)]])))

(defn table-row [cursor owner opts]
  (reify
    om/IInitState
    (init-state [_]
      {:c (chan (sliding-buffer 100))
       :value (-> cursor :weeks first :w)})
    om/IWillMount
    (will-mount [_]
      (let [mult-chan (om/get-shared owner :mult-chan)
            c         (om/get-state owner :c)
            week      (header)]
        (tap mult-chan c)
        (go-loop []
          (let [event-chan         (om/get-state owner :c)
                {:keys [event v]}  (<! event-chan)]
            (when v
              (om/set-state! owner :value v)
              (om/update! week :selected-week (common/unparse-date v "yyyy-MM-dd"))))
          (recur))))
    om/IRenderState
    (render-state [_ state]
      (let [mouseover (:mouseover state)
            value     (:value state)]
        (html
         (let [value (common/timestamp->value (:value state) "w" (:weeks cursor))]
           [:tr
            [:td (-> cursor :author :login)]
            [:td (aget value "a")]
            [:td (aget value "d")]
            [:td (aget value "c")]]))))
    om/IWillUnmount
    (will-unmount [_]
      (untap (om/get-shared owner :mult-chan) (om/get-state owner :c)))))

(defn table-stats-view [cursor owner]
  (om/component
   (html
    [:div.table-view
     [:table.table {:width "100%"}
      [:thead
       [:tr [:th "Author"] [:th "Additions"] [:th "Deletions"] [:th "Total Commits"]]]
      [:tbody
       (om/build-all table-row cursor)]]])))

(defmulti code-frequency-stats (fn [cursor owner] (-> cursor :view :current)))

(defmethod code-frequency-stats :charts [cursor owner]
  (om/component
   (html
    [:div
     (when (seq (:data cursor))
       (om/build chart-stats-view {:data (:data cursor) :div (:div cursor)} {:fn (parse-contributors :charts)}))])))

(defmethod code-frequency-stats :table [cursor owner]
  (om/component
   (html
    [:div
     (om/build table-stats-view (:data cursor) {:fn (parse-contributors :table)})])))

(defmethod code-frequency-stats :cards [cursor owner]
  (om/component
   (html
    [:div
     (om/build team-members-stats-view (:data cursor) {:fn (parse-contributors :table)})])))

(defn send-messages [urls]
  (doseq [{:keys [k url refresh-rate]} urls]
    (send-message [k {:url url :refresh-rate refresh-rate}])))

(defn toggles [cursor owner]
  (om/component
   (let [current-view (-> cursor :view :current)
         week         (:selected-week (om/observe owner (header)))]
     (html
      (b/toolbar {}
       (html [:div.pull-left (str "Showing week: " week)])
       (b/button {:bs-size "xsmall" :class (str "pull-right " (if (= current-view :table) "active" ""))
                  :key :table
                  :on-click (fn [e]
                              (om/update! cursor [:view :current] :table))}
                 "Table")
       (b/button {:bs-size "xsmall" :class (str "pull-right " (if (= current-view :cards) "active" ""))
                  :key :cards
                  :on-click (fn [e]
                              (om/update! cursor [:view :current] :cards))}
                 "Cards")
       (b/button {:bs-size "xsmall" :class (str "pull-right " (if (= current-view :charts) "active" ""))
                  :key :charts
                  :on-click (fn [e]
                              (om/update! cursor [:view :current] :charts))}
                 "Charts"))))))

(defn toggles2 [cursor owner]
  (om/component
   (let [current-interaction (:current cursor)]
     (html
      (b/toolbar {}
                 (html [:div.pull-left "Additions and deletions per week"])
                 (b/button {:bs-size "xsmall" :class (str "pull-right " (if (= current-interaction :hover) "active" ""))
                            :key :table
                            :on-click (fn [e]
                                        (om/update! cursor :current :hover))}
                           "Hover")
                 (b/button {:bs-size "xsmall" :class (str "pull-right " (if (= current-interaction :click) "active" ""))
                            :key :cards
                            :on-click (fn [e]
                                        (om/update! cursor :current :click))}
                           "Click"))))))

(defn refresh-stats [cursor owner]
  (om/component
   (html
    (let [{:keys [refresh-rate updated-at]} cursor]
      [:div
       (str "Refresh rate: " (str (common/refresh-rate->hours refresh-rate) "h. Updated at "
                                  (if (not (nil? updated-at))
                                    (common/unparse-date updated-at "yyyy-MM-dd HH:mm")
                                    "N/A")))]))))

(defn repository-view [cursor owner {:keys [url]}]
  (reify
    om/IDidMount
    (did-mount [_]
      (send-messages [{:k :dashboard/github-issues          :url (str url "/issues?state=open") :refresh-rate 21600000}
                      {:k :dashboard/github-pulls           :url (str url "/pulls?state=open") :refresh-rate 21600000}
                      {:k :dashboard/github-code-frequency  :url (str url "/stats/code_frequency") :refresh-rate 21600000}
                      {:k :dashboard/github-contributors    :url (str url "/stats/contributors") :refresh-rate 21600000}]))
    om/IRender
    (render [_]
      (html
       [:div.col-md-12
        [:div.row.view
         [:div.col-md-6
          (p/panel {:header (om/build toggles2 (-> cursor :code-frequency :event-toggle))
                    :footer (om/build refresh-stats (-> cursor :code-frequency))}
                   (om/build charts/stacked-bar-chart (:code-frequency cursor)
                             {:opts {:id "stacked-bar-chart"}}))]
         [:div.col-md-6
          (p/panel {:header (om/build toggles (:contributors cursor))
                    :footer (om/build refresh-stats (-> cursor :contributors))}
                   (om/build code-frequency-stats (:contributors cursor)))]]
        [:div.row.view
         [:div.row.row-centered
          [:div.col-sm-4.col-centered
           (om/build numbers/simple-stats-card (:issues cursor))]
          [:div.col-sm-4.col-centered
           (om/build numbers/simple-stats-card (:pulls cursor))]]]]))))

(defn clock [cursor owner {:keys [formatter]}]
  (reify
    om/IWillMount
    (will-mount [_]
      (js/setInterval (fn [] (om/update! cursor :time (common/get-time formatter))) 1000))
    om/IRender
    (render [_]
      (html
       [:div (:time cursor)]))))

(defn dashboard [app owner]
  (reify
    om/IRender
    (render [_]
      (html
       [:div
        [:div.container-fluid.dashboard
         ;; Jumbotron
         (r/jumbotron {} (html [:h1 "Team Dashboard" [:small.pull-right (om/build clock (:clock app))]]))
         ;; Repository View
         (om/build repository-view (-> app :repository)
                   {:opts {:url "https://api.github.com/repos/mastodonc/kixi.hecuba"}})]]))))

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
  (let [event-chan (chan (sliding-buffer 100))]
    (om/root
     application
     app-state
     {:target (. js/document (getElementById "app"))
      :shared {:event-chan event-chan :mult-chan (mult event-chan)}})))
