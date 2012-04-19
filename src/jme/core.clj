(ns jme.core
  (:import com.jme3.math.Vector3f)
  (:import com.jme3.math.Quaternion)
  (:import com.jme3.scene.Geometry)
  (:import com.jme3.scene.Node)
  (:import com.jme3.scene.shape.Box)
  (:import com.jme3.material.Material)
  (:import com.jme3.asset.TextureKey)
  (:import com.jme3.app.SimpleApplication)
  (:import [com.jme3.system AppSettings JmeSystem])
  (:import (java.util.logging Level Logger))
)

(defmacro eat-exceptions
  [& forms]
  `(try ~@forms (catch Exception e# (.printStackTrace e#))))

(defn warn-logging []
  (.setLevel (Logger/getLogger "com.jme3") Level/WARNING))

(def asset-manager (JmeSystem/newAssetManager (.getResource (.getContextClassLoader (Thread/currentThread)) "com/jme3/asset/Desktop.cfg")))

(defn material
  ([obj material-path texture-path]
    (let [mat (Material. asset-manager material-path)]
      (.setTexture mat "ColorMap" (.loadTexture asset-manager (TextureKey. texture-path)))
      (.setMaterial obj mat))
    obj)
  ([obj] (material obj "Common/MatDefs/Misc/Unshaded.j3md" "Textures/Terrain/BrickWall/BrickWall.jpg")))

(defn position
  ([obj x y] (position obj x y 0))
  ([obj x y z]
    (.setLocalTranslation obj (Vector3f. x y z))
    obj))

(defn rotate [obj x y z w]
  (.rotate obj (Quaternion. x y z w))
  obj)

(defn geometry
  ([mesh] (geometry "geometry" mesh))
  ([name mesh] (Geometry. name mesh)))

(defn box
  ([name l w h] (geometry name (Box. l w h)))
  ([l w h] (box "box" l w h))
  ([] (box 1 1 1)))

(defn node
  ([children]
    (node "node" children))
  ([name children]
    (let [n (Node. name)]
      (dorun (map #(.attachChild n %) children))
      n)))

(defn world [root-node setup-fn update-fn]
  (doto
    (proxy [SimpleApplication] []
      (simpleInitApp []
        (eat-exceptions
          (.attachChild (.getRootNode this) root-node)
          (if setup-fn
            (setup-fn this))))
      (simpleUpdate [tpf]
        (eat-exceptions
          (update-fn this tpf))))
    (.setShowSettings false)))

(defn view [obj]
  (.start (world (node [obj]) (fn [world] "") (fn [world tpf] ""))))