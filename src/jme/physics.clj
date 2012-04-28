(ns jme.physics
  (:import com.jme3.math.Vector3f)
  (:import com.jme3.bullet.control.CharacterControl)
  (:import com.jme3.bullet.collision.shapes.CapsuleCollisionShape)
  (:import com.jme3.bullet.util.CollisionShapeFactory)
  (:import com.jme3.bullet.control.RigidBodyControl))


(defn mesh-shape [node]
  (CollisionShapeFactory/createMeshShape node))

(defn rigid-body-control
  ([shape] (rigid-body-control shape 0))
  ([shape mass] (RigidBodyControl. shape mass)))

(defn character-control [character-spatial step-height jump-speed fall-speed gravity]
  (let [control (CharacterControl. (CapsuleCollisionShape. 1.5 6 1) step-height)]
    (.addControl character-spatial control)
    (doto control
      (.setJumpSpeed jump-speed)
      (.setFallSpeed fall-speed)
      (.setGravity gravity))))

(defn location
  ([character x y z] (doto character (.setPhysicsLocation (Vector3f. x y z))))
  ([character] (let [v (.getPhysicsLocation character)] {:x (.x v) :y (.y v) :z (.z v)})))