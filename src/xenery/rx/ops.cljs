(ns xenery.rx.ops
  (:require
   [applied-science.js-interop :as j]
   ["rxjs/operators" :as rxjs-ops])
  (:refer-clojure
   :exclude
   [map
    empty
    take
    filter
    delay
    merge
    concat]))

(defn- map [transform-fn]
  (rxjs-ops/map #(transform-fn %)))

(defn- map-to [value]
  (rxjs-ops/mapTo value))

(defn- tap [{:keys [next complete error]}]
  (rxjs-ops/tap next error complete))

(defn- switch-map [project-fn]
  (rxjs-ops/switchMap project-fn))

(defn- merge-map [project-fn]
  (rxjs-ops/mergeMap project-fn))

(defn- concat-map [project-fn]
  (rxjs-ops/concatMap project-fn))

(defn- start-with [& vals]
  (j/apply rxjs-ops :startWith (into-array vals)))

(defn- distinct-until-changed [comp]
  (rxjs-ops/distinctUntilChanged (or comp =)))

(defn- throttle-time [duration]
  (rxjs-ops/throttleTime duration))

(defn- buffer-time [duration]
  (rxjs-ops/bufferTime duration))

(defn- debounce-time [duration]
  (rxjs-ops/debounceTime duration))

(defn- timeout [period]
  (rxjs-ops/timeout period))

(defn- with-latest-from
  ([o]
   (rxjs-ops/withLatestFrom o))
  ([o project-fn]
   (rxjs-ops/withLatestFrom o project-fn)))

(defn- share []
  (rxjs-ops/share))

(defn- share-replay [buffer-size]
  (rxjs-ops/shareReplay (or buffer-size 1)))

(defn- take [count]
  (rxjs-ops/take count))

(defn- skip [count]
  (rxjs-ops/skip count))

(defn- filter [pred-fn]
  (rxjs-ops/filter pred-fn))

(defn- delay [duration]
  (rxjs-ops/delay duration))

(defn- catch-error [project-fn]
  (rxjs-ops/catchError project-fn))

(defn- finalize [finally-fn]
  (rxjs-ops/finalize finally-fn))

(def ^:private pipe-ops
  (atom
   {:map                     map
    :map-to                  map-to
    :tap                     tap
    :catch-error             catch-error
    :finalize                finalize
    :switch-map              switch-map
    :merge-map               merge-map
    :concat-map              concat-map
    :start-with              start-with
    :throttle-time           throttle-time
    :buffer-time             buffer-time
    :debounce-time           debounce-time
    :timeout                 timeout
    :with-latest-from        with-latest-from
    :take                    take
    :skip                    skip
    :filter                  filter
    :delay                   delay
    :distinct-until-changed  distinct-until-changed
    :share                   share
    :share-replay            share-replay}))

(defn register-op! [k f]
  (swap! pipe-ops assoc k f))

(defn- pipe [o & ops]
  (j/apply o :pipe (into-array ops)))

(defn- op-vec->rx-op
  [[op & args]]
  (let [pipe-op (op @pipe-ops)]
    (assert (some? pipe-op) (str "Op not found: " op))
    (apply pipe-op args)))

(defn => [o & op-vecs]
  (if (not-empty op-vecs)
    (->> op-vecs
         (remove nil?)
         (cljs.core/map op-vec->rx-op)
         (apply pipe o))
    o))
