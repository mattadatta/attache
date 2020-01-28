(ns attache.landing.components.scenes.root
  (:require
   [xenery.core :as xe :refer [defnx]]))

(defnx RecentsListItem
  [_ {:keys [clicked?]} {:keys [set-clicked?]}]
  {:state {:clicked? false}}
  [:div
   {:style
    {:height 50
     :margin-bottom 8
     :background-color (if clicked? "red" "cyan")}
    :on-click #(set-clicked? not)}])

(defnx RecentsList
  [_]
  [:div
   {:style
    {:position "absolute"
     :left 0 :top 0 :right 0 :bottom 0}}
   (into
    [:div
     {:style
      {:position "absolute"
       :left 0 :top 0 :right 0 :bottom 0
       :padding 14
       :overflowX "hidden"
       :overflowY "scroll"}}]
    (repeat 20 [RecentsListItem]))])

(defnx RecentsPanel
  [{:keys [style]}]
  [:div
   {:style
    {:width "calc(100% - 56px)"
     :height "calc(100% - 56px)"
     :margin 28
     :display "flex"
     :flex-direction "column"}}
   [:span
    {:style
     {:margin-left 4 :margin-bottom 8
      :font-family "Alfa Slab One"
      :font-size 28
      :text-align "left"}}
    "recents"]
   [:div
    {:style
     {:flex 1
      :background-color "rgba(44,44,44,1)"
      :border-radius 4
      :position "relative"}}
    [RecentsList]]])

(defnx Root
  [_]
  [:div
   {:style
    {:display "flex"
     :height "100%"
     :flex-direction "column"
     :background-color "rgba(32,32,32,255)"}}
   [:div
    {:style
     {:flex (/ 3 10)
      :display "flex"
      :flex-direction "column"
      :align-items "center"
      :justify-content "center"}}
    [:span
     {:style
      {:font-family "Alfa Slab One"
       :font-size 48
       :text-align "center"}}
     "attaché"]
    [:span
     {:style
      {:margin-top 16
       :font-family "Montserrat"
       :font-weight 500
       :font-size 16
       :text-align "center"}}
     "a graphical remote debugger for Nintendo Switch"]]
   [:div
    {:style
     {:display "flex"
      :flex (/ 7 10)}}
    [:div
     {:style
      {:flex 1
       :height "100%"}}
     [RecentsPanel]]
    [:div
     {:style
      {:flex 1
       :height "100%"}}]]])
