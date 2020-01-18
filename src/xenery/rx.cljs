(ns attache.xenery.rx
  (:require
   [applied-science.js-interop :as j]

   ["rxjs" :as rxjs]
   ["rxjs/operators" :as rxjs-ops])
  (:refer-clojure
   :exclude
   [empty
    take
    filter
    delay
    merge
    concat]))

(def Observable rxjs/Observable)
(def Subject rxjs/Subject)
(def BehaviorSubject rxjs/BehaviorSubject)
(def ReplaySubject rxjs/ReplaySubject)
(def Subscription rxjs/Subscription)

(def never rxjs/NEVER)

(defn clj->js-observer [{:keys [next complete error]}]
  (->> {:next next :complete complete :error error}
       (remove (comp nil? second))
       (into {})
       (clj->js)))

(defn create [on-subscribe]
  (.create Observable on-subscribe))

(defn defer [on-subscribe]
  (rxjs/defer on-subscribe))

(defn empty []
  (rxjs/empty))

(defn interval [period]
  (rxjs/interval period))

(defn of [& values]
  (j/apply rxjs :of (clj->js (into-array values))))

(defn from [ish]
  (rxjs/from ish))

(defn create-publish-subject []
  (Subject.))

(defn create-behavior-subject
  ([]
   (create-behavior-subject {:initial-value nil}))
  ([{:keys [initial-value]}]
   (BehaviorSubject. initial-value)))

(defn create-replay-subject
  ([]
   (create-replay-subject {:size 1}))
  ([{:keys [size]}]
   (ReplaySubject. (or size 1))))

;; Piping

(declare =>)

(defn- -map [transform-fn]
  (rxjs-ops/map #(transform-fn %)))

(defn- prev-and-next
  ([]
   (prev-and-next nil))
  ([initial-value]
   (fn [source]
     (defer
       (fn []
         (let [prev (atom initial-value)]
           (=> source
               [:map (fn [element]
                       (let [prev-val @prev]
                         (reset! prev element)
                         [prev-val element]))])))))))

(defn- map-to [value]
  (rxjs-ops/mapTo value))

(defn- tap [{:keys [next complete error]}]
  (rxjs-ops/tap next error complete))

(defn- debug [key]
  (tap {:next
        (fn [element]
          (.warn js/console (str "[" (str key) "]/N: " (if element (str element) "_nil"))))
        :complete
        (fn []
          (.warn js/console (str "[" (str key) "]/C")))
        :error
        (fn [error]
          (.error js/console (str "[" (str key) "]/E: " (str error))))}))

(defn- tap-next [next-fn]
  (tap {:next next-fn}))

(defn- tap-complete [complete-fn]
  (tap {:complete complete-fn}))

(defn- tap-error [error-fn]
  (tap {:error error-fn}))

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

(defn delay [duration]
  (rxjs-ops/delay duration))

(defn merge [& obs]
  (j/apply rxjs :merge (clj->js (into-array obs))))

(defn zip [& obs]
  (j/apply rxjs :zip (clj->js (into-array obs))))

(defn combine-latest [& obs]
  (j/apply rxjs :combineLatest (clj->js (into-array obs))))

(defn concat [& obs]
  (j/apply rxjs :concat (clj->js (into-array obs))))

(defn- catch-error [project-fn]
  (rxjs-ops/catchError project-fn))

(defn- catch-error-return [element]
  (catch-error #(of element)))

(defn- catch-error-complete []
  (catch-error #(empty)))

(defn- catch-error-do [do-fn]
  (catch-error #(do (do-fn %) (empty))))

(defn- finalize [finally-fn]
  (rxjs-ops/finalize finally-fn))

(defn subscribe!
  ([o]
   (subscribe! o {}))
  ([o observer]
   (.subscribe o (if (map? observer)
                   (clj->js-observer observer)
                   observer))))

(defn subscribe-next! [o on-next]
  (subscribe! o {:next on-next}))

(defn unsubscribe! [sub]
  (.unsubscribe sub))

(defn subscribe-using!
  ([o atom]
   (subscribe-using! o atom {}))
  ([o atom observer]
   (let [sub (subscribe! o observer)]
     (if-let [subs @atom]
       (.add subs sub)
       (reset! atom (let [s (Subscription.)]
                      (.add s sub)
                      s))))))

(defn unsubscribe-using! [atom]
  (when-let [sub @atom]
    (unsubscribe! sub)
    (reset! atom nil)))

(defn compose-subs [& subs]
  (let [parent (Subscription.)]
    (doseq [sub subs]
      (.add parent sub))
    parent))

(defn next! [o element]
  (.next o element))

(defn complete! [o]
  (.complete o))

(defn error! [o error]
  (.error o error))

(defn defer-promise [promise-fn]
  (defer (fn [] (from (promise-fn)))))

(defn to-promise [o]
  (j/call o :toPromise))

(defn- try-next [tag]
  (fn [source]
    (create
     (fn [o]
       (subscribe!
        source
        {:next
         (fn [element]
           (try
             (next! o element)
             (catch :default e
               (throw (js/Error. (str "[" (str tag) "]: Error while sending element:" e))))))
         :complete
         #(complete! o)
         :error
         #(error! o %)})))))

(defn- swapping-bind! [o update-fn]
  (fn [source]
    (=> source
        [:with-latest-from o (fn [curr next]
                               [(update-fn curr next) next])]
        [:tap-next
         (fn [[updated _]]
           (next! o updated))]
        [:map second])))

(def pipe-ops
  {:map                     -map
   :map-to                  map-to
   :prev-and-next           prev-and-next
   :tap                     tap
   :tap-next                tap-next
   :tap-complete            tap-complete
   :tap-error               tap-error
   :debug                   debug
   :try-next                try-next
   :catch-error             catch-error
   :catch-error-return      catch-error-return
   :catch-error-complete    catch-error-complete
   :catch-error-do          catch-error-do
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
   :swapping-bind!          swapping-bind!
   :take                    take
   :skip                    skip
   :filter                  filter
   :delay                   delay
   :distinct-until-changed  distinct-until-changed
   :share                   share
   :share-replay            share-replay})

(defn pipe [o & ops]
  (j/apply o :pipe (clj->js (into-array ops))))

(defn- op-vec->rx-op
  [[op & args]]
  (let [pipe-op (op pipe-ops)]
    (assert (some? pipe-op) (str "Op not found: " op))
    (apply pipe-op args)))

(defn => [o & op-vecs]
  (if (not-empty op-vecs)
    (->> (conj op-vecs [:catch-error-do
                        (fn [error]
                          (.warn js/console (str "Got error with: \n" error)))])
         (remove nil?)
         (map op-vec->rx-op)
         (apply pipe o))
    o))
