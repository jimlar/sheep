(ns sheep.world
  (:require [jme.core :as jme]))

(def map-data ["#############"
               "# #       1  "
               "# # # #######"
               "#   # #      "
               "#######      "])

(defn wall-positions [map-data]
  (apply concat
    (map-indexed
      (fn [y row] (filter #(not (nil? %))
                    (map-indexed (fn [x chr] (if (= chr \#) [x y] nil)) row)))
      map-data)))

(defn player-position [map-data]
  (first
    (apply concat
      (map-indexed
        (fn [y row] (filter #(not (nil? %))
                      (map-indexed (fn [x chr] (if (= chr \1) [x y] nil)) row)))
        map-data))))

(defn wall-box [x y]
  (jme/position (jme/material (jme/box 0.5 0.5 1)) x y 0))

(defn labyrinth []
  (jme/node "Labyrinth" (map #(wall-box (first %) (second %)) (wall-positions map-data))))

(defn player
  ([pos] (player (first pos) (second pos)))
  ([x y]
    (jme/position (jme/material (jme/sphere 0.5) "Common/MatDefs/Misc/Unshaded.j3md" "Textures/Terrain/Rocky/RockyTexture.jpg") x y 0)))

(defn setup [world]
  (jme/node [(labyrinth) (player (player-position map-data))]))

(defn update [world tpf])
