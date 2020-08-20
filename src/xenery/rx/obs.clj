(ns xenery.rx.obs)

(defmacro defer-promise [& body]
  `(xenery.rx.obs/defer (fn [] (xenery.rx.obs/from (do ~@body)))))
