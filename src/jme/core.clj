(ns jme.core
  (:import [com.jme3.math Vector3f Quaternion])
  (:import [com.jme3.scene Geometry Node CameraNode])
  (:import [com.jme3.scene.shape Box Sphere])
  (:import com.jme3.scene.control.CameraControl)
  (:import com.jme3.material.Material)
  (:import com.jme3.asset.TextureKey)
  (:import com.jme3.app.SimpleApplication)
  (:import com.jme3.light.DirectionalLight)
  (:import com.jme3.input.KeyInput)
  (:import com.jme3.asset.plugins.FileLocator)
  (:import [com.jme3.input.controls Trigger KeyTrigger ActionListener AnalogListener])
  (:import [com.jme3.system AppSettings JmeSystem])
  (:import [java.util.logging Level Logger]))

(defmacro eat-exceptions
  [& forms]
  `(try ~@forms (catch Exception e# (.printStackTrace e#))))

(defn warn-logging []
  (.setLevel (Logger/getLogger "com.jme3") Level/WARNING))

(defn info-logging []
  (.setLevel (Logger/getLogger "com.jme3") Level/INFO))

(warn-logging)

(def asset-manager
  (doto
    (JmeSystem/newAssetManager (.getResource (.getContextClassLoader (Thread/currentThread)) "com/jme3/asset/Desktop.cfg"))
    (.registerLocator "assets" FileLocator)))

(defn load-model [path]
  (.loadModel asset-manager path))

(defn directional-light [node x y z]
  (doto node (.addLight (doto (DirectionalLight.) (.setDirection (Vector3f. x y z))))))

(defn material
  ([obj material-path texture-path]
    (let [mat (Material. asset-manager material-path)]
      (.setTexture mat "ColorMap" (.loadTexture asset-manager (TextureKey. texture-path)))
      (doto obj (.setMaterial mat))))
  ([obj texture-path] (material obj "Common/MatDefs/Misc/Unshaded.j3md" texture-path))
  ([obj] (material obj "Textures/Terrain/BrickWall/BrickWall.jpg")))

(defn position
  ([obj] (let [v (.getLocalTranslation obj)] {:x (.x v) :y (.y v) :z (.z v)}))
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

(defn sphere
  ([name radius] (geometry name (Sphere. 32 32 (float radius))))
  ([radius] (sphere "sphere" radius))
  ([] (sphere 0.5)))

(defn node
  ([children]
    (node "node" children))
  ([name children]
    (let [n (Node. name)]
      (dorun (map #(.attachChild n %) children))
      n)))

(defn attach [node obj]
  (.attachChild node obj)
  obj)

(defn default-settings []
  (doto (AppSettings. true)
    (.setHeight 800)
    (.setWidth 1280)))

(defn static-integer? [#^java.lang.reflect.Field field]
  (and (java.lang.reflect.Modifier/isStatic (.getModifiers field))
    (integer? (.get field nil))))

(defn integer-constants [class]
  (let [integer-fields (filter static-integer? (.getFields class))]
    (into (sorted-map)
      (zipmap (map #(.get % nil) integer-fields)
        (map #(.getName %) integer-fields)))))

(defn all-keys []
  (let [inputs (integer-constants KeyInput)]
    (zipmap (map (fn [field] (keyword (.toLowerCase (.replaceAll field "_" "-")))) (vals inputs))
            (map (fn [val] (KeyTrigger. val)) (keys inputs)))))

(alter-var-root #'all-keys memoize)

(defn init-keymappings [world key-map]
  (let [input-manager (.getInputManager world)
        listener (proxy [AnalogListener] []
                    (onAnalog [binding value tpf]
                      (eat-exceptions
                        (if-let [react ((keyword (subs binding 1)) key-map)]
                          (react this value)))))]
    (eat-exceptions
      (vec
        (for [k (keys key-map)]
          (doto input-manager
            (.addMapping (str k) (into-array Trigger [(k (all-keys))]))
            (.addListener listener (into-array String [(str k)]))))))))

(defn world [key-map setup-fn update-fn]
  (let [app (proxy [SimpleApplication ActionListener] []
              (simpleInitApp []
                (eat-exceptions
;;                  (org.lwjgl.input.Mouse/setGrabbed false)
                  (attach (.getRootNode this) (setup-fn this))
                  (init-keymappings this key-map)
                  (.setMoveSpeed (.getFlyByCamera this) (float 7))
                  (.setRotationSpeed (.getFlyByCamera this) (float 2))))
              (simpleUpdate [tpf]
                          (eat-exceptions
                            (update-fn this tpf))))]
    (doto app
      (.setShowSettings false)
      (.setSettings (default-settings)))))

(defn view [obj]
  (.start (world {} (fn [world] obj) (fn [world tpf] ""))))

(defn camera-node [world obj]
  (.setEnabled (.getFlyByCamera world) false)
  (attach
    obj
      (doto (CameraNode. "Camera" (.getCamera world))
        (.setControlDir CameraControl$ControlDirection/SpatialToCamera)
        (.lookAt (.getLocalTranslation obj) Vector3f/UNIT_Y))))
