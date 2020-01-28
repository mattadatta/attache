(ns xenery.hooks.state
  (:require
   ["react" :as React]))

(defn useState
  [initial]
  (let [[value updater]
        (React/useState initial)
        
        wrapped-updater
        (React/useCallback
         (fn updater-wrap
           [next-state]
           (if-not (ifn? next-state)
             (updater next-state)
             (updater
              (fn update [current-state]
                (let [new-state (next-state current-state)]
                  (if (= current-state new-state)
                    current-state
                    new-state))))))
         #js [updater])]
    [value wrapped-updater]))
