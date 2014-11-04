(ns pumpkin.widgets.charts
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [om.core :as om :include-macros true]
            [cljs.core.async :refer [<! >! chan put!]]
            [sablono.core :as html :refer-macros [html]]
            [pumpkin.common :refer (log)]))

(enable-console-print!)

(defn default-size [id]
  (let [e (.getElementById js/document id)
        x (.-clientWidth e)
        y (.-clientHeight e)]
    {:width x :height y}))

(defn sort-by-field [field]
  (fn [x y]
    (let [v1 (first (aget x field))
          v2 (first (aget y field))]
      (cond
     (> v1 v2) -1
     (< v1 v2) 1
     (= v1 v2) 0))))

(defn- draw-chart [data div {:keys [id bounds x-axis y-axis plot series color event-type]}]
  (let [width        (or (:width div) (:width (default-size id)))
        height       (or (:height div) (:height (default-size id)))
        data         (aget data "data")
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
    (.draw dimple-chart (when (= event-type :click) 1000))
    (-> legend .-shapes (.selectAll "text") (.text (clojure.string/capitalize y-axis)))
    ;; Rotate x-axis labels
    (.attr (.selectAll (.-shapes x) "text") "transform" " rotate(45 10 25)")
    (.attr (.selectAll (.-shapes x) "text") "x" 4)
    (.attr (.selectAll (.-shapes x) "text") "y" 4)))

(defn bar-chart
  "Simple bar chart done using dimple.js"
  [{:keys [data div]} owner {:keys [id] :as opts}]
  (reify
    om/IWillMount
    (will-mount [_]
      (.addEventListener js/window
                         "resize" (fn []
                                    (let [e (.getElementById js/document id)
                                          x (.-clientWidth e)
                                          y (.-clientHeight e)]
                                      (om/update! div :size {:width x :height y})))))
    om/IRender
    (render [_]
      (html
       [:div.chart {:id id}]))
    om/IDidMount
    (did-mount [_]
      (let [n (.getElementById js/document id)]
        (while (.hasChildNodes n)
          (.removeChild n (.-lastChild n))))
      (draw-chart data div opts))
    om/IDidUpdate
    (did-update [_ _ _]
      (let [n (.getElementById js/document id)]
        (while (.hasChildNodes n)
          (.removeChild n (.-lastChild n))))
      (draw-chart data div opts))))

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

(defn- draw [data hover-chan size id event-type]
  (let [margin          {:top 10 :right 40 :bottom 55 :left 50}
        width            (-> (or (:width size) (:width (default-size id)))  (- (:left margin) (:right margin)))
        height           (-> (or (:height size) (:height (default-size id))) (- (:top margin) (:bottom margin)))
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
        x-axis          (-> js/d3 (.-svg) (.axis) (.scale x-scale) (.orient "bottom")
                            (.ticks 52) (.tickSize 0) (.tickFormat (-> js/d3 .-time (.format "%d %B"))))
        ;; Y
        y-scale         (-> js/d3 .-scale (.linear) (.domain (to-array [0 max-value])) (.range (to-array [height 0])))
        y-axis          (-> js/d3 (.-svg) (.axis) (.scale y-scale) (.tickSize 10) (.orient "left"))
        ;; Main chart
        svg             (create-svg (str "#" id) width height margin)]

    ;; X axis and labels
    (-> svg
        (.append "g")
        (.attr "class" "x axis")
        (.attr "transform" (str "translate(0," height ")"))
        (.call x-axis)
        (.selectAll "text")
        (.style "text-anchor" "start")
        (.attr "dx" "5")
        (.attr "dy" ".25em")
        (.attr "transform" (fn [d] "rotate(45)")))

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
          (.on "click" (fn [d] (when (= event-type :click)
                                 (let [bar     (js* "this")
                                       get-val (fn [n] (-> n  .-baseVal .-value))
                                       [x y]   (js->clj (-> js/d3 (.mouse bar)))
                                       x1      (-> bar .-x get-val)]
                                   (put! hover-chan {:event :click :v (.-x d) :d d})))))
          (.on "mouseout" (fn [_] (when (= event-type :hover)
                                    (put! hover-chan {:event :mouseout}))))
          (.on "mouseover" (fn [d] (when (= event-type :hover)
                                     (let [[x y] (js->clj (-> js/d3 (.mouse (js* "this"))))]
                                       (put! hover-chan {:event :mouseover :v (.-x d) :d d})))))
          ;; Transitions
          (.attr "height" 0)
          (.attr "y" height)
          (.transition)
          (.duration 1000)
          (.ease "cubic-in-out")
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
        (.attr "x" (- width 18))
        (.attr "height" 8)
        (.attr "width" 8)
        (.style "fill" colors))
       ;; Labels
    (-> (.selectAll svg ".legend")
        (.append "text")
        (.attr "x" (- width 6))
        (.attr "y" 8)
        (.style "font-size" 10)
        (.style "text-anchor" "start")
        (.text (fn [d] (if (= d 0) "Additions" "Deletions"))))))

(defn stacked-bar-chart
  "Stacked bar chart done in d3.js"
  [chart owner {:keys [id]}]
  (reify
    om/IWillMount
    (will-mount [_]
      (.addEventListener js/window
                       "resize" (fn []
                                  (let [e (.getElementById js/document id)
                                        x (.-clientWidth e)
                                        y (.-clientHeight e)]
                                    (om/update! chart :size {:width x :height y})))))
    om/IRender
    (render [_]
      (html [:div.chart {:id id}]))
    om/IDidMount
    (did-mount [_]
      (let [n (.getElementById js/document id)]
        (while (.hasChildNodes n)
          (.removeChild n (.-lastChild n))))
      (let [data         (:data chart)
            event-type   (-> chart :event-toggle :current)
            event-chan   (om/get-shared owner :event-chan)]
        (draw data event-chan (:size chart) id event-type)))
    om/IDidUpdate
    (did-update [_ _ _]
      (let [n (.getElementById js/document id)]
        (while (.hasChildNodes n)
          (.removeChild n (.-lastChild n))))
      (let [data         (:data chart)
            event-type   (-> chart :event-toggle :current)
            event-chan   (om/get-shared owner :event-chan)]
        (draw data event-chan (:size chart) id event-type)))))
