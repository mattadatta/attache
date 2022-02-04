(ns attache.landing.components.scenes.root
  (:require
   [applied-science.js-interop :as j]
   [xenery.core :as xe :refer [defnx]]
   [xenery.rx.obs :as rxc]
   [xenery.rx.ops :as rxo]
   [xenery.rx.util-ops]
   [xenery.hooks.react :as hr]
   [xenery.hooks.rx :as hrx]))

(defnx InputBox
  [{:keys []}]
  (let [[text set-text] (hr/use-state "")]
    [:input
     {:type "text"
      :name "Things"
      :value text
      :on-change #(-> % (j/get-in [:target :value]) set-text)}]))

(defnx RecentsListItem
  [{:keys [item-key]}]
  (let [[is-clicked? set-clicked] (hr/use-state false)]
    [:div
     {:style
      {:height 50
       :margin-bottom 8
       :background-color (if is-clicked? "red" "cyan")}
      :on-click #(set-clicked not)}
     [:span
      {:style
       {:margin-left 4 :margin-bottom 8
        :font-family "Alfa Slab One"
        :font-size 28
        :text-align "left"}}
      (str "Item: " item-key)]]))

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
       :overflow-x "hidden"
       :overflow-y "scroll"}
      :on-scroll nil}]
    (map #(do [RecentsListItem {:item-key (str %)}]) (range 20)))])

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
     {:flex 3
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
     {:flex 1}}
    [InputBox]]
   [:div
    {:style
     {:display "flex"
      :flex 7}}
    [:div
     {:style
      {:flex 1
       :height "100%"}}
     [RecentsPanel]]
    [:div
     {:style
      {:flex 1
       :height "100%"}}]]])
