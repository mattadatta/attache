(ns xenery.hooks.rx
  (:require
   [xenery.rx.obs :as rxc]
   [xenery.hooks.react :as hr]))

(defn useObservable
  ([o]
   (let [[state set-state]
         (hr/useState nil)]
     (hr/useLayoutEffect
      (fn []
        (let [sub
              (rxc/subscribe-next! o #(set-state %))]
          #(rxc/unsubscribe! sub))))
     state))
  ([o deps]
   (let [[state set-state]
         (hr/useState nil)]
     (hr/useLayoutEffect
      (fn []
        (let [sub
              (rxc/subscribe-next! o #(set-state %))]
          #(rxc/unsubscribe! sub)))
      deps)
     state)))