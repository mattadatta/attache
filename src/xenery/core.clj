(ns xenery.core)

(defmacro defnc [n props-bindings & body]
  `(do
     (def
       ~n
       (fn ~n [props#]
          (let [~props-bindings [(xenery.util/shallow-props->clj props#)]]
            (xenery.core/vec-to-elem (do ~@body)))))
     (when goog/DEBUG
       (applied-science.js-interop/assoc! ~n "displayName" ~(str *ns* "/" n)))))
