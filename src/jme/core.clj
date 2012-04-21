(ns jme.core
  (:import [com.jme3.math Vector3f Quaternion])
  (:import [com.jme3.scene Geometry Node])
  (:import com.jme3.scene.shape.Box)
  (:import com.jme3.material.Material)
  (:import com.jme3.asset.TextureKey)
  (:import com.jme3.app.SimpleApplication)
  (:import com.jme3.light.DirectionalLight)
  (:import [com.jme3.system AppSettings JmeSystem])
  (:import [java.util.logging Level Logger]))

(defmacro eat-exceptions
  [& forms]
  `(try ~@forms (catch Exception e# (.printStackTrace e#))))

(defn warn-logging []
  (.setLevel (Logger/getLogger "com.jme3") Level/WARNING))

(warn-logging)

(defn info-logging []
  (.setLevel (Logger/getLogger "com.jme3") Level/INFO))

(def asset-manager (JmeSystem/newAssetManager (.getResource (.getContextClassLoader (Thread/currentThread)) "com/jme3/asset/Desktop.cfg")))

(defn load-model [path]
  (.loadModel asset-manager path))

(defn directional-light [node x y z]
  (doto node (.addLight (doto (DirectionalLight.) (.setDirection (Vector3f. x y z))))))

(defn material
  ([obj material-path texture-path]
    (let [mat (Material. asset-manager material-path)]
      (.setTexture mat "ColorMap" (.loadTexture asset-manager (TextureKey. texture-path)))
      (doto obj (.setMaterial mat))))
  ([obj] (material obj "Common/MatDefs/Misc/Unshaded.j3md" "Textures/Terrain/BrickWall/BrickWall.jpg")))

(defn position
  ([obj x y] (position obj x y 0))
  ([obj x y z] (doto obj (.setLocalTranslation (Vector3f. x y z)))))

(defn rotate
  ([obj x y z w] (doto obj (.rotate (Quaternion. x y z w))))
  ([obj x y z] (doto obj (.rotate x y z))))

(defn scale [obj x y z]
  (doto obj (.scale x y z)))

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

(defn attach [node obj]
  (.attachChild node obj))

(defn default-settings []
  (doto (AppSettings. true)
    (.setHeight 800)
    (.setWidth 1280)))

(defn world [setup-fn update-fn]
  (doto
    (proxy [SimpleApplication] []
      (simpleInitApp []
        (eat-exceptions
          (attach (.getRootNode this) (setup-fn this))))
      (simpleUpdate [tpf]
        (eat-exceptions
          (update-fn this tpf))))
    (.setShowSettings false)
    (.setSettings (default-settings))))

(defn view [obj]
  (.start (world (fn [world] obj) (fn [world tpf] ""))))

