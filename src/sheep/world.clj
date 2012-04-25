(ns sheep.world
  (:require [jme.core :as jme]))

(def map-data ["#############"
               "# #       1  "
               "# # # #######"
               "#   # #      "
               "#######      "])

(defn positions-of [map-data chr]
  (apply concat
    (map-indexed
      (fn [y row] (filter #(not (nil? %))
                    (map-indexed (fn [x c] (if (= c chr) [x y] nil)) row)))
      map-data)))

(defn wall-positions [map-data]
  (positions-of map-data \#))

(defn player-position [map-data]
  (first (positions-of map-data \1)))

(defn wall-box [x y]
;;  (jme/position (jme/material (jme/box 0.5 0.5 1) "textures/grass/grass.png") x y 0))
  (jme/position (jme/material (jme/box 0.5 0.5 1)) x y 0))

(defn labyrinth []
  (jme/node "Labyrinth" (map #(wall-box (first %) (second %)) (wall-positions map-data))))

(defn create-player
  ([pos] (create-player (first pos) (second pos)))
  ([x y]
    (jme/position (jme/node [(jme/material (jme/sphere 0.3) "Textures/Terrain/Rocky/RockyTexture.jpg")]) x y 0)))

(defn ground []
  (jme/material (jme/position (jme/geometry "Ground" (jme/quad 50 50)) -25 -1 25)))

(defonce player (atom nil))
(def player-speed 3)

(defn move-left [world value]
  (let [pos (jme/position @player)]
    (jme/position @player (- (:x pos) (* value player-speed )) (:y pos) (:z pos))))

(defn move-right [world value]
  (let [pos (jme/position @player)]
    (jme/position @player (+ (:x pos) (* value player-speed)) (:y pos) (:z pos))))

(defn move-up [world value]
  (let [pos (jme/position @player)]
    (jme/position @player (:x pos) (+ (:y pos) (* value player-speed )) (:z pos))))

(defn move-down [world value]
  (let [pos (jme/position @player)]
    (jme/position @player (:x pos) (- (:y pos) (* value player-speed )) (:z pos))))

(defn keymap []
  {
    :key-left move-left
    :key-right move-right
    :key-up move-up
    :key-down move-down
  })

(defn setup-camera [world]
  (jme/position (jme/rotate (jme/camera-node world @player) 0 1.2 0) 0 0 5))

(defn setup [world]
  (jme/disable-flyby-cam world)
  (swap! player (fn [ignore pos] (create-player pos)) (player-position map-data))
  (setup-camera world)
  (jme/node [(labyrinth) @player (ground)]))

(defn update [world tpf])


(defn setup-and-start-world []
  (doto
    (jme/world (keymap) setup update)
    (.start)))