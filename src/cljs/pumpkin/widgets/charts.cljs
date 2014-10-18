(ns pumpkin.widgets.charts
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [om.core :as om :include-macros true]
            [cljs.core.async :refer [<! >! chan put!]]
            [sablono.core :as html :refer-macros [html]]
            [pumpkin.common :refer (log)]))

(enable-console-print!)

(defn sort-by-field [field]
  (fn [x y]
    (let [v1 (first (aget x field))
          v2 (first (aget y field))]
      (cond
     (> v1 v2) -1
     (< v1 v2) 1
     (= v1 v2) 0))))

(defn- draw-chart [cursor {:keys [div bounds x-axis y-axis plot series color]}]
  (let [{:keys [id width height]} div
        data         (clj->js (get cursor "data"))
        Chart        (.-chart js/dimple)
        svg          (.newSvg js/dimple (str "#" id) width height)
        dimple-chart (.setBounds (Chart. svg) (:x bounds) (:y bounds) (:width bounds) (:height bounds))
        x            (.addCategoryAxis dimple-chart "x" x-axis)
        y            (.addMeasureAxis dimple-chart "y" y-axis)
        s            (.addSeries dimple-chart series plot (clj->js [x y]))
        color-fn     (-> js/dimple .-color)
        legend       (.addLegend dimple-chart "10%" "5%" "80%" "10%" "right")]
    (aset s "data" (clj->js data))
    (aset dimple-chart "defaultColors" (to-array [(new color-fn color)]))
    (.addOrderRule x (sort-by-field y-axis))
    (.draw dimple-chart)
    (-> legend .-shapes (.selectAll "text") (.text y-axis))
    ;; Rotate x-axis labels
    (.attr (.selectAll (.-shapes x) "text") "transform" " rotate(45 10 25)")
    (.attr (.selectAll (.-shapes x) "text") "x" 4)
    (.attr (.selectAll (.-shapes x) "text") "y" 4)))

(defn bar-chart
  "Simple bar chart done using dimple.js"
  [cursor owner {:keys [chart] :as opts}]
  (reify
    om/IRender
    (render [_]
      (let [{:keys [id width height]} (:div chart)]
        (html
         [:div.chart {:id id}])))
    om/IDidMount
    (did-mount [_]
      (let [id (-> chart :div :id)
            n (.getElementById js/document id)]
        (while (.hasChildNodes n)
          (.removeChild n (.-lastChild n))))
      (draw-chart cursor chart))
    om/IDidUpdate
    (did-update [_ _ _]
      (let [id (-> chart :div :id)
            n (.getElementById js/document id)]
        (while (.hasChildNodes n)
          (.removeChild n (.-lastChild n))))
      (draw-chart cursor chart))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SVG

(defn create-svg [div width height margin]
  (-> js/d3 (.select div) (.append "svg:svg")
      (.attr #js {:width  (+ width (:left margin) (:right margin))
                  :height (+ height (:top margin) (:bottom margin))})
      (.append "svg:g")
      (.attr #js {:transform (str "translate(" (:left margin) "," (:top margin) ")")})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Min and max

(defn min-max-dates
  [data]
  (reduce (fn [acc {:keys [timestamp]}]
            (if (:min-date acc)
              (assoc acc
                :min-date (min timestamp (:min-date acc))
                :max-date (max timestamp (:max-date acc)))
              (assoc acc
                :min-date timestamp
                :max-date timestamp)))
          {}
          data))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Draw chart

(defn- draw [data hover-chan]
  (let [margin          {:top 10 :right 10 :bottom 55 :left 50}
        width           (-> (.getElementById js/document "chart") .-clientWidth (- (:left margin) (:right margin)))
        height          (-> (.getElementById js/document "chart") .-clientHeight (- (:top margin) (:bottom margin)))
        ;; Grouped by type and stacked
        grouped         (mapv (fn [[k v]] {:data v :name (name k)}) (group-by :type data))
        series          (map :name grouped)
        timestamps      (map :timestamp data)
        grouped-js      (clj->js grouped)
        dataset         (.map grouped-js (fn [d] (.map (.-data d) (fn [o i] #js {:y (.-value o) :x (.-timestamp o)}))))
        stack           (-> js/d3 .-layout (.stack))
        stacked         (stack dataset)
        ;; Min and max for y
        max-value       (-> js/d3 (.max stacked (fn [s] (-> js/d3 (.max s (fn [d] (+ (.-y0 d) (.-y d))))))))
        min-value       (-> js/d3 (.min stacked (fn [s] (-> js/d3 (.min s (fn [d] (+ (.-y0 d) (.-y d))))))))
        {:keys [min-date max-date]} (min-max-dates data)
        data            (clj->js data)
        ;; Color
        colors          (-> js/d3 .-scale (.ordinal) (.range (to-array ["#4575b4" "#d62728"])))
        ;; X
        x-scale         (-> js/d3 .-time (.scale) (.domain (to-array [min-date max-date])) (.range (to-array [0 width])))
        x-axis          (-> js/d3 (.-svg) (.axis) (.scale x-scale) (.orient "bottom"))
        ;; Y
        y-scale         (-> js/d3 .-scale (.linear) (.domain (to-array [0 max-value])) (.range (to-array [height 0])))
        y-axis          (-> js/d3 (.-svg) (.axis) (.scale y-scale) (.tickSize 10) (.orient "left"))
        ;; Main chart
        svg             (create-svg "#chart" width height margin)
        vertical        (-> (-> js/d3 (.select ".chart"))
                            (.append "div")
                            (.attr "class" "remove")
                            (.style "position" "absolute")
                            (.style "z-index" "19")
                            (.style "width" 0)
                            (.style "height" (str (+ height (:top margin)) "px"))
                            (.style "top" (str (+ (:top margin) (:bottom margin) 5)  "px"))
                            (.style "bottom" (str 0 "px"))
                            (.style "left" "0px")
                            (.style "background" "grey"))]

    ;; X axis and labels
    (-> svg
        (.append "g")
        (.attr "class" "x axis")
        (.attr "transform" (str "translate(0," height ")"))
        (.call x-axis))

    ;; Y-axis and labels
    (-> svg
        (.append "g")
        (.attr "class" "y axis")
        (.call y-axis))

    (doto (.selectAll svg ".frequency")
      (-> (.data stacked)
          (.enter)
          (.append "g")
          (.attr "class" "frequency")
          (.style "fill" (fn [d i] (colors i)))
          (.style "stroke" (fn [d i] (-> js/d3 (.rgb (colors i)) (.darker))))
          ;; Rects
          (.selectAll "rect")
          (.data (fn [d] d))
          (.enter)
          (.append "rect")
          (.on "click" (fn [d] (let [bar     (js* "this")
                                     get-val (fn [n] (-> n  .-baseVal .-value))
                                     [x y]   (js->clj (-> js/d3 (.mouse bar)))
                                     x1      (-> bar .-x get-val)
                                     width   (-> bar .-width get-val)
                                     left    (+ x1 (/ width 2))]
                                 (.style vertical "width" "2px")
                                 (.style vertical "left" (str (+ left (:left margin) (:right margin) 5) "px"))
                                 (put! hover-chan {:event :click :v (.-x d) :d d}))))
          (.on "mouseout" (fn [_] (put! hover-chan {:event :mouseout})))
          (.on "mouseover" (fn [d] (let [[x y] (js->clj (-> js/d3 (.mouse (js* "this"))))]
                                     (put! hover-chan {:event :mouseover :v (.-x d) :d d}))))
          ;; Transitions
          (.transition)
          (.duration 500)
          (.ease "cubic")
          (.attr "height" 0)
          (.transition)
          (.duration 500)
          ;; Bars
          (.attr "fill-opacity" .7)
          (.attr "x" (fn [d] (x-scale (aget d "x"))))
          (.attr "y" (fn [d] (y-scale (+ (aget d "y0") (aget d "y")))))
          (.attr "height" (fn [d] (- (y-scale (aget d "y0")) (y-scale (+ (aget d "y0") (aget d "y"))))))
          (.attr "width" (* (/ width (count data)) 2))))

    ;; Legend
    (-> (.selectAll svg ".legend")
        (.data (-> colors (.domain) (.slice) (.reverse)))
        (.enter)
        (.append "g")
        (.attr "class" "legend")
        (.attr "transform" (fn [d i] (str "translate(0," (* i 10) ")")))
        ;; Rects
        (.append "rect")
        (.attr "x" (- width 38))
        (.attr "height" 8)
        (.attr "width" 8)
        (.style "fill" colors)
        ;; Labels
        (.append "text")
        (.attr "x" (- width 38))
        (.attr "y" 30)
        (.attr "dy" ".35em")
        (.style "text-anchor" "end")
        (.text (fn [d] d)))

))

(defn stacked-bar-chart
  "Stacked bar chart done in d3.js"
  [chart owner opts]
  (reify
    om/IRender
    (render [_]
      (html [:div.chart {:id "chart" }])) ;; TODO pass id in opts
    om/IDidMount
    (did-mount [_]
      (let [n (.getElementById js/document "chart")]
        (while (.hasChildNodes n)
          (.removeChild n (.-lastChild n))))
      (let [data (:data chart)]
        (draw data (:hover-chan opts))))))
