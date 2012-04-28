(ns sheep.world
  (:require [jme.core :as jme]
            [jme.physics :as phys]))

(def map-data ["###############"
               "# #       p   #"
               "# # # ####### #"
               "#   # #     # #"
               "#######     ###"])

(defn positions-of [map-data chr]
  (apply concat
    (map-indexed
      (fn [y row] (filter #(not (nil? %))
                    (map-indexed (fn [x c] (if (= c chr) [x y] nil)) row)))
      map-data)))

(defn wall-positions [map-data]
  (positions-of map-data \#))

(defn player-start-pos [map-data]
  (first (positions-of map-data \p)))

(defn wall-box [x y]
;;  (jme/position (jme/material (jme/box 0.5 0.5 1) "textures/grass/grass.png") x y 0))
  (jme/position (jme/material (jme/box 0.5 0.5 1)) x y 0))

(defn labyrinth []
  (jme/node "Labyrinth"
    (conj
      (map #(wall-box (first %) (second %)) (wall-positions map-data))
      (jme/material (jme/position (jme/geometry "Ground" (jme/quad 50 50)) -25 -25 -1) "Textures/Terrain/Rocky/RockyTexture.jpg"))))

(defn create-player
  ([pos] (create-player (first pos) (second pos)))
  ([x y]
    (let [
           player-node (jme/node [(jme/material (jme/sphere 0.3) "Textures/Terrain/Rocky/RockyTexture.jpg")])
           control (phys/character-control player-node 0.05 20 30 30)
           ]
      (phys/location control x y 0)
      {:control control :node player-node})))

(defonce player (atom nil))
(def player-speed 3)

(defn player-node []
  (:node @player))

(defn player-control []
  (:control @player))

(defn player-pos []
  (phys/location (player-control)))


(defn move-left [world value]
  (let [pos (player-pos)]
    (phys/location (player-control ) (- (:x pos) (* value player-speed)) (:y pos) (:z pos))))

;(.setWalkDirection (player-control) (.multLocal (.clone (.getLeft (.getCamera world))) (float 0.4))))


(defn move-right [world value]
  (let [pos (player-pos)]
    (phys/location (player-control ) (+ (:x pos) (* value player-speed)) (:y pos) (:z pos))))

(defn move-up [world value]
  (let [pos (player-pos)]
    (phys/location (player-control ) (:x pos) (+ (:y pos) (* value player-speed )) (:z pos))))

(defn move-down [world value]
  (let [pos (player-pos)]
    (phys/location (player-control ) (:x pos) (- (:y pos) (* value player-speed )) (:z pos))))

(defn keymap []
  {
    :key-left move-left
    :key-right move-right
    :key-up move-up
    :key-down move-down
  })

(defn setup-camera [world]
  (jme/disable-flyby-cam world)
  (jme/position (jme/rotate (jme/camera-node world (player-node) (player-control)) 0 1.2 0) 0 0 2.5))

(defn setup [world physics-space]
  (let [labyrinth (labyrinth)
        landscape (phys/rigid-body-control (phys/mesh-shape labyrinth))]
    (swap! player (fn [ignore pos] (create-player pos)) (player-start-pos map-data))
    (setup-camera world)
    (jme/add-control labyrinth landscape)
    (.add physics-space landscape)
    (jme/node [labyrinth (player-node)])))

(defn update [world tpf])


(defn setup-and-start-world []
  (doto
    (jme/world (keymap) setup update)
    (.start)))
