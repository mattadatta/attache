(ns xenery.core
  (:require
   ["react" :as React]
   
   [xenery.hooks.react]

   [xenery.util :as util])
  (:require-macros
   [xenery.core]))

(defonce tag-registry (atom {}))

(defn register-tag! [tag impl]
  (swap! tag-registry assoc tag impl))

(defn key->type [tag]
  (if-let [t (get @tag-registry tag nil)]
    t
    (name tag)))

(register-tag! :<> React/Fragment)

(defn default-error-renderer
  ([e ee]
   [:div
    {:style
     {:background-color "rgba(154,60,60,1)"}}
    [:span
     {:style
      {:font-family "Montserrat"
       :font-size 16
       :color "rgba(255,255,255,1)"
       :text-align "left"}}
     (str "Error: " e "\nAlso got error using default error renderer: " ee)]])
  ([e]
   [:div
    {:style
     {:background-color "rgba(154,60,60,1)"}}
    [:span
     {:style
      {:font-family "Montserrat"
       :font-size 16
       :color "rgba(255,255,255,1)"
       :text-align "left"}}
     (str "Error: " e)]]))

(defonce -error-renderer (atom default-error-renderer))

(defn error-renderer []
  (or @-error-renderer default-error-renderer))

(defn set-error-renderer! [f]
  (assert (fn? f) "Supplied error-renderer is not a function")
  (reset! -error-renderer f))

(declare vec-to-elem)

(defn as-element [x]
  (cond
    (vector? x)
    (vec-to-elem x)

    :else
    x))

(defn- maybe-wrap-child-fn [c]
  (if (fn? c)
    (fn [& args]
      (let [ret (apply c args)]
        (if (vector? ret)
          (as-element ret)
          ret)))
    c))

(defn- element-type [t]
  (cond
    (keyword? t)
    (key->type t)
    
    :else
    t))

(defn vec-to-elem [[t props & children]]
  (when-let [class (element-type t)]
    (let [js-props (or (util/props->js props) #js{})]
      (case (count children)
        0 (React/createElement class js-props)
        1 (React/createElement class js-props (-> children first maybe-wrap-child-fn as-element))
        (.apply React/createElement nil
                (reduce
                 (fn [o child]
                   (.push o (as-element child))
                   o)
                 #js[class js-props]
                 children))))))
