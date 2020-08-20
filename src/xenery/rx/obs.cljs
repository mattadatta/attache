(ns xenery.rx.obs
  (:require
   [applied-science.js-interop :as j]

   ["rxjs" :as rxjs])
  (:require-macros
   [xenery.rx.obs])
  (:refer-clojure
   :exclude
   [map
    empty
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


;; Observable creation

(defn create [on-subscribe]
  (.create Observable on-subscribe))

(defn defer [on-subscribe]
  (rxjs/defer on-subscribe))

(defn empty []
  (rxjs/empty))

(defn from [ish]
  (rxjs/from ish))

(defn interval [period]
  (rxjs/interval period))

(defn of [& values]
  (j/apply rxjs :of (clj->js (into-array values))))


;; Observable combination

(defn combine-latest [& obs]
  (j/apply rxjs :combineLatest (into-array obs)))

(defn concat [& obs]
  (j/apply rxjs :concat (into-array obs)))

(defn fork-join [& obs]
  (if (map? (first obs))
    (j/call rxjs :forkJoin (clj->js (first obs)))
    (j/apply rxjs :forkJoin (into-array obs))))

(defn merge [& obs]
  (j/apply rxjs :merge (into-array obs)))

(defn race [& obs]
  (j/apply rxjs :race (into-array obs)))

(defn zip [& obs]
  (j/apply rxjs :zip (into-array obs)))


;; Subjects

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

(defn subscribe!
  ([o]
   (subscribe! o {}))
  ([o observer]
   (.subscribe o (if (map? observer)
                   (->> observer
                        (remove (comp nil? second))
                        (into {})
                        (clj->js))
                   observer))))

(defn subscribe-next! [o on-next]
  (subscribe! o {:next on-next}))

(defn unsubscribe! [sub]
  (.unsubscribe sub))

(defn next! [o element]
  (.next o element))

(defn complete! [o]
  (.complete o))

(defn error! [o error]
  (.error o error))

(defn to-promise [o]
  (j/call o :toPromise))
