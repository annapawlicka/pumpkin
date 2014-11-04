(ns pumpkin.widgets.numbers
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [pumpkin.common :refer [log] :as common]))

(enable-console-print!)

(defn simple-stats-card [cursor owner]
  (reify
    om/IRender
    (render [_]
      (html
       (let [{:keys [title value refresh-rate updated-at]} cursor]
         [:div.number-widget
          [:div.panel.panel-default
           [:div.panel-body.rounded
            [:h5 title]
            [:h4 value]
            [:p (str "Refresh rate: " (str (common/refresh-rate->hours refresh-rate) "h"))]
            [:p (str "Updated at: "  (if (not (nil? updated-at))
                                       (common/unparse-date updated-at "yyyy-MM-dd HH:mm")
                                       "N/A"))]]]])))))
