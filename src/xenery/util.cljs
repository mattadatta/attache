(ns xenery.util
  (:require
   [inflections.core :as infl]
   [applied-science.js-interop :as j]))

(defn memoize-1 [f]
  (let [mem (atom {})]
    (fn [arg]
      (let [v (get @mem arg)]
        (if-not (nil? v)
          v
          (let [ret (f arg)]
            (swap! mem assoc arg ret)
            ret))))))

(defn js-memoize-1 [f]
  (let [mem #js {}]
    (fn [arg]
      (let [k
            (cond
              (keyword? arg)
              (name keyword)
              
              (string? arg)
              arg
              
              :else
              (str arg))
            
            v (j/get mem k)]
        (if-not (nil? v)
          v
          (let [ret (f arg)]
            (j/assoc! mem k ret)
            ret))))))

(def key->js-prop
  (memoize-1
   (fn [s] (infl/camel-case (name s) :lower))))

(defn props->js [x]
  (cond
    (identical? (type x) js/Object)
    x

    (or (keyword? x)
        (symbol? x))
    (name x)

    (map? x)
    (reduce-kv #(j/assoc! %1 (key->js-prop %2) (props->js %3)) #js{} x)

    (coll? x)
    (clj->js x)

    (ifn? x)
    (fn [& args]
      (apply x args))

    :else
    (clj->js x)))

(def js-prop->key
  (memoize-1
   (fn [x]
     (-> x
         name
         infl/hyphenate
         keyword))))

(defn shallow-props->clj
  [x]
  (cond
    (array? x)
    (persistent!
     (reduce #(conj! %1 %2)
             (transient []) x))

    (identical? (type x) js/Object)
    (persistent!
     (reduce
      (fn [r k]
        (let [kw (js-prop->key k)]
          (if (= kw :style)
            (assoc! r kw (shallow-props->clj (j/get x k)))
            (assoc! r kw (j/get x k)))))
      (transient {}) (js-keys x)))
    
    :else
    x))

(defn map-keys
  "Apply the provided function 'f' to each key in map 'm'."
  [f m]
  (persistent!
   (reduce-kv (fn [a k v] (assoc! a (f k) v)) (transient {}) m)))

(defn map-values
  "Apply the provided function 'f' to each value in map 'm'."
  [f m]
  (persistent!
   (reduce-kv (fn [a k v] (assoc! a k (f v))) (transient {}) m)))

(defn transform-keys
  [xform x]
  (cond
    (map? x)
    (->> x
         (map-keys xform)
         (map-values (partial transform-keys xform)))

    (coll? x)
    (->> x
         (map (partial transform-keys xform))
         (into (empty x)))

    :else x))

(defn transform-values
  [xform x]
  (cond
    (map? x)
    (->> x
         (map-values (partial transform-values xform)))

    (coll? x)
    (->> x
         (map (partial transform-values xform))
         (into (empty x)))

    :else (xform x)))