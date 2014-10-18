(ns pumpkin.widgets.numbers
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [om-bootstrap.panel :as p]
            [om.core :as om :include-macros true]
            [cljs.core.async :as async :refer [<! >! chan mult tap sliding-buffer]]
            [sablono.core :as html :refer-macros [html]]
            [pumpkin.colorbrewer :as colorbrewer]
            [cljs-time.format :as tf]
            [cljs-time.coerce :as tc]
            [pumpkin.common :refer [log] :as common]))

(enable-console-print!)

(defn changing-color-number [cursor owner {:keys [scale]}]
  (reify
    om/IRender
    (render [_]
      (html
       (let [{:keys [title value refresh-rate updated-at]} cursor
             {:keys [good-value bad-value]} scale]
         [:div.number-widget
          [:div.panel.panel-default
           [:div.panel-body.rounded {:style {:background-color (colorbrewer/brewer value
                                                                                   good-value
                                                                                   bad-value
                                                                                   7
                                                                                   :RdYlBu)}}
            [:h5 title]
            [:h4 value]
            [:p (str "Refresh rate: " refresh-rate)]
            [:p (str "Updated at: "  (if (not (nil? updated-at))
                                       (common/unparse-date updated-at "yyyy-MM-dd")
                                       "N/A"))]]]])))))

(defn team-member
  "Shows team member's info and stats."
  [cursor owner opts]
  (reify
    om/IInitState
    (init-state [_]
      {:value {}
       :mouseover false})
    om/IWillMount
    (will-mount [_]
      (go-loop []
        (let [event-chan         (om/get-state owner :chan)
              {:keys [event v]}  (<! event-chan)
              bisect             (-> js/d3 (.bisector (fn [d] (aget d "w"))) .-right)]
          (cond
           (= event :mouseover) (let [data (-> (mapv #(assoc % :w (common/get-date-from-week (:w %))) (-> @cursor :weeks)) clj->js)
                                      index  (bisect data v 1)
                                      value  (js->clj (aget data index))]
                                  (om/set-state! owner :value value)
                                  (om/set-state! owner :mouseover true))
           (= event :mouseout) (om/set-state! owner :mouseover false)))
        (recur)))
    om/IRenderState
    (render-state [_ state]
      (let [mouseover (:mouseover state)
            value (:value state)
            week-start (if-let [w (get value "w")] (common/unparse-date w "yyyy-MM-dd") "")
            {:keys [author]} cursor]
        (html
         [:div {:style {:font-size "80%"}}
          [:div.panel.panel-default
           [:div.panel-heading
            [:h4.panel-title [:p (:login author)] [:p (when-let [uri (:avatar_url author)]
                                [:img.img-thumbnail.table-image.pull-right
                                 {:src uri :style {:max-width "25%"}}])]]]
           [:div.panel-body {:style {:height 90}}
            (if mouseover
              [:div
               [:p week-start]
               [:p "Additions: " (get value "a")]
               [:p "Deletions: " (get value "d")]]
              [:div
               [:p "Total commits: " (:total cursor)]])]]])))))

(defn get-time [formatter]
  (let [date      (js/Date.)
        f         (or formatter "dth MMMM yyyy HH:mm:ss")
        formatted (common/unparse-date date f)]
    formatted))

(defn clock [cursor owner {:keys [formatter]}]
  (reify
    om/IWillMount
    (will-mount [_]
      (js/setInterval (fn [] (om/update! cursor :time (get-time formatter))) 1000))
    om/IRender
    (render [_]
      (html
       [:div (:time cursor)]))))
