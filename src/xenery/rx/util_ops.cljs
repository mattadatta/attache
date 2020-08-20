(ns xenery.rx.util-ops
  (:require
   [xenery.rx.obs :as rxc]
   [xenery.rx.ops :as rxo]))

(defn- prev-and-next
  ([]
   (prev-and-next nil))
  ([initial-value]
   (fn [source]
     (rxc/defer
       (fn []
         (let [prev (atom initial-value)]
           (rxo/=> source
                   [:map (fn [element]
                           (let [prev-val @prev]
                             (reset! prev element)
                             [prev-val element]))])))))))

(rxo/register-op! :prev-and-next prev-and-next)

(defn- debug [key]
  (fn [source]
    (rxo/=> source
            [:tap
             {:next
              (fn [element]
                (.warn js/console (str "[" (str key) "]/N: " (if element (str element) "_nil"))))
              :complete
              (fn []
                (.warn js/console (str "[" (str key) "]/C")))
              :error
              (fn [error]
                (.error js/console (str "[" (str key) "]/E: " (str error))))}])))

(rxo/register-op! :debug debug)

(defn- tap-next [next-fn]
  (fn [source]
    (rxo/=> source
            [:tap {:next next-fn}])))

(rxo/register-op! :tap-next tap-next)

(defn- tap-complete [complete-fn]
  (fn [source]
    (rxo/=> source
            [:tap {:complete complete-fn}])))

(rxo/register-op! :tap-complete tap-complete)

(defn- tap-error [error-fn]
  (fn [source]
    (rxo/=> source
            [:tap {:error error-fn}])))

(rxo/register-op! :tap-error tap-error)

(defn- catch-error-return [element]
  (fn [source]
    (rxo/=> source
            [:catch-error #(rxc/of element)])))

(rxo/register-op! :catch-error-return catch-error-return)

(defn- catch-error-complete []
  (fn [source]
    (rxo/=> source
            [:catch-error #(rxc/empty)])))

(rxo/register-op! :catch-error-complete catch-error-complete)

(defn- catch-error-do [do-fn]
  (fn [source]
    (rxo/=> source
            [:catch-error #(do (do-fn %) (rxc/empty))])))

(rxo/register-op! :catch-error-do catch-error-do)

(defn- try-next [tag]
  (fn [source]
    (rxc/create
     (fn [o]
       (let [sub
             (rxc/subscribe!
              source
              {:next
               (fn [element]
                 (try
                   (rxc/next! o element)
                   (catch :default e
                     (throw (js/Error. (str "[" (str tag) "]: Error while sending element:" e))))))
               :complete
               #(rxc/complete! o)
               :error
               #(rxc/error! o %)})]
         #(rxc/unsubscribe! sub))))))

(rxo/register-op! :try-next try-next)

(defn- swapping-bind! [o update-fn]
  (fn [source]
    (rxo/=> source
            [:with-latest-from o (fn [curr next]
                                   [(update-fn curr next) next])]
            [:tap-next
             (fn [[updated _]]
               (rxc/next! o updated))]
            [:map second])))

(rxo/register-op! :swapping-bind! swapping-bind!)
