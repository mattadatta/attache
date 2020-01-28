(ns xenery.core)

(defn- fnx* [n [params & body]]
  (let [attrs
        (or (when (and (next body) (map? (first body)))
              (first body)) 
            (meta params))

        body
        (if attrs (next body) body)

        state
        (when (map? (:state attrs))
          (:state attrs))

        props-binding
        (if (or (= (first params) '_)
                (nil? (first params)))
          'nil
          `(xenery.util/shallow-props->clj ~(symbol "**js-props**")))]

    `(let [func# 
           (fn ~n [~(symbol "**js-props**")]
             ~(if state
                `(let [state-hooks#
                       (->> ~state
                            (map (fn [[state-key# initial-value#]]
                                   [state-key# (xenery.hooks.state/useState initial-value#)]))
                            (into {}))
                       
                       ~(symbol "**xe-state-updaters**")
                       (->> state-hooks#
                            (xenery.util/map-keys #(keyword (str "set-" (name %))))
                            (xenery.util/map-values second))
                       
                       ~params [~props-binding (xenery.util/map-values first state-hooks#) ~(symbol "**xe-state-updaters**")]]
                   (xenery.core/vec-to-elem (do ~@body)))
                `(let [~params [~props-binding]]
                   (xenery.core/vec-to-elem (do ~@body)))))]
       (goog.object/set func# "displayName" ~(str *ns* "/" n))
       func#)))

(defmacro fnx [& sig]
  (let [name
        (if (symbol? (first sig))
          (first sig)
          (let [{:keys [line column]}
                (meta &form)]
            (str "L" line "-C" column)))
        
        sig
        (if (symbol? (first sig)) (rest sig) sig)]
    (fnx* name sig)))

(defmacro defnx [& sig]
  (let [name (first sig)]
    `(def ~name ~(fnx* name (rest sig)))))

(defmacro set-state! [k v]
  (let [name
        (let [{:keys [line column]}
              (meta &form)]
          (str "L" line "-C" column))]
    `(let [state-updaters#
           ~(symbol "**xe-state-updaters**")]
       (assert (map? state-updaters#) (str "Unable to find state updaters for set-state! on " ~name))
       (let [updater#
             ((keyword (str "set-" (name ~k))) state-updaters#)]
         (assert (ifn? updater#) (str "Unable to find updater for state " ~k " on " ~name))
         (fn []
           (updater# ~v))))))
