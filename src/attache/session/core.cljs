(ns attache.session.core
  (:require
   [xenery.core :as xe]
   ["react-dom" :as ReactDom]))

(defn start []
  (js/console.log "landing - start")
  (ReactDom/render
   (xe/as-element
    [:p
     {}
     "Well this isn't done yet"])
   (. js/document getElementById "app")))

(defn init []
  (js/console.log "landing - init")
  (start))

(defn stop []
  (js/console.log "landing - stop"))