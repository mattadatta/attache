(ns attache.landing.core
  (:require
   [xenery.core :as xe]
   ["react-dom" :as ReactDom]
   
   [attache.landing.components.scenes.root :as root]
   ))

(defn start []
  (js/console.log "landing - start")
  (ReactDom/render
   (xe/as-element
    [root/Root
     {}])
   (. js/document getElementById "app")))

(defn init []
  (js/console.log "landing - init")
  (start))

(defn stop []
  (js/console.log "landing - stop"))