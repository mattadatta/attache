(ns attache.main.core
  (:require 
   ["electron" :as e :refer [app BrowserWindow globalShortcut]]
   ["path" :as path]
   ["url" :as url]
   
   [applied-science.js-interop :as j]))

(defonce win-ref (atom nil))

(defn create-window []
  (let [win
        (BrowserWindow. #js {:width 800
                             :height 600
                             :webPreferences
                             #js {:scrollBounce true}})

        url
        (url/format #js {:pathname (path/join js/__dirname "index.html")
                         :protocol "file:"
                         :slashes true})]

    (.loadURL win url)
    (reset! win-ref win)
    (.on win "closed"
         (fn [_]
           (reset! win-ref nil)))
    (.register 
     globalShortcut 
     "Command+D" 
     #(.. win -webContents (openDevTools)))))

(defn maybe-quit []
  (when (not= js/process.platform "darwin")
    (.quit app)))

(defn maybe-create-window []
  (when-not @win-ref
    (create-window)))

(defn main []
  (.on app "ready" create-window)
  (.on app "activate" maybe-create-window)
  (.on app "window-all-closed" maybe-quit))
