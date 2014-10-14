(ns pumpkin.widgets.numbers
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]))

(enable-console-print!)

(defn simple-number [cursor owner {:keys [color]}]
  (reify
    om/IRender
    (render [_]
      (html
       (let [{:keys [title value refresh-rate updated-at]} cursor]
         [:div.col-xs-3
          [:div.dummy.widget
           [:div {:class (str "in " color)}
            [:h2 title]
            [:h3 value]
            [:p (str "Refresh rate: " refresh-rate)] ;; TODO fn in common for displaying it in days hours, mins etc.
            [:p (str "Updated at: "  updated-at)]]]])))))
