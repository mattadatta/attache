(ns xenery.core)

(defn- fnx* [n [params & body]]
  (let [attrs
        (when (and (next body) (map? (first body)))
          (first body))

        body
        (if attrs (next body) body)

        attrs
        (or attrs (meta params))

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
                                   [state-key# (xenery.hooks.react/useState initial-value#)]))
                            (into {}))
                       
                       state-updaters#
                       (->> state-hooks#
                            (xenery.util/map-keys #(keyword (str "set-" (name %))))
                            (xenery.util/map-values second))
                       
                       ~params [~props-binding (xenery.util/map-values first state-hooks#) state-updaters#]]
                   (xenery.core/vec-to-elem
                    (try
                      (do ~@body)
                      (catch :default e#
                        (try
                          ((xenery.core/error-renderer) e#)
                          (catch :default e2#
                            (xenery.core/default-error-renderer e# e2#)))
                        ))))
                `(let [~params [~props-binding]]
                   (xenery.core/vec-to-elem (do ~@body)))))]
       (goog.object/set func# "displayName" ~(str *ns* "/" n))
       func#)))

(defn- fnxr* [n [params & body]]
  (let [attrs
        (when (and (next body) (map? (first body)))
          (first body))

        body
        (if attrs (next body) body)

        attrs
        (or attrs (meta params))

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
                                   [state-key# (xenery.hooks.react/useState initial-value#)]))
                            (into {}))

                       state-updaters#
                       (->> state-hooks#
                            (xenery.util/map-keys #(keyword (str "set-" (name %))))
                            (xenery.util/map-values second))

                       ~params [~props-binding (xenery.util/map-values first state-hooks#) state-updaters#]]
                   (xenery.core/vec-to-elem
                    (try
                      (do ~@body)
                      (catch :default e#
                        (try
                          ((xenery.core/error-renderer) e#)
                          (catch :default e2#
                            (xenery.core/default-error-renderer e# e2#)))))))
                `(let [~params [~props-binding]]
                   (xenery.core/vec-to-elem (do ~@body)))))]
       (goog.object/set func# "displayName" ~(str *ns* "/" n))
       func#)))

(defmacro fnxr [& sig]
  (let [name
        (if (symbol? (first sig))
          (first sig)
          (let [{:keys [line column]}
                (meta &form)]
            (str "L" line "-C" column)))

        sig
        (if (symbol? (first sig)) (rest sig) sig)]
    (fnxr* name sig)))

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

; (defmacro rxs [v]
;   (let [name
;         (let [{:keys [line column]}
;               (meta &form)]
;           (str "L" line "-C" column))]
;     `(let [[val set-val] (xenery.hooks.react/useState nil)]
;        (xenery.hooks.react/useLayoutEffect
;         (fn []
;           (xenery.rx))))))
