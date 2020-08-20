(ns xenery.hooks.react
  (:require
   ["react" :as React]))

(defn useState
  [initial]
  (let [[state set-state]
        (React/useState initial)
        
        wrapped-set-state
        (React/useCallback
         (fn set-state-maybe-retain-ref
           [next-state]
           (if-not (ifn? next-state)
             (set-state next-state)
             (set-state
              (fn update-state [current-state]
                (let [new-state (next-state current-state)]
                  (if (= current-state new-state)
                    current-state
                    new-state))))))
         #js [set-state])]
    [state wrapped-set-state]))

(defn- wrap-effect [f]
  (fn fn-or-undef []
    (let [x (f)]
      (if (fn? x)
        x
        js/undefined))))

(defn useEffect
  ([f]
   (React/useEffect (wrap-effect f) #js []))
  ([f deps]
   (React/useEffect (wrap-effect f) (to-array deps))))

(defn useLayoutEffect
  ([f] 
   (React/useLayoutEffect (wrap-effect f) #js []))
  ([f deps] 
   (React/useLayoutEffect (wrap-effect f) (to-array deps))))
