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
  (jme/position (jme/material (jme/box 0.5 0.5 1)) x y 0))

(defn labyrinth []
  (jme/node "Labyrinth" (map #(wall-box (first %) (second %)) (wall-positions map-data))))

(defn create-player
  ([pos] (create-player (first pos) (second pos)))
  ([x y]
    (jme/position (jme/material (jme/sphere 0.5) "Common/MatDefs/Misc/Unshaded.j3md" "Textures/Terrain/Rocky/RockyTexture.jpg") x y 0)))

(def player (atom nil))

(defn move-left [])

(defn move-right [])

(defn keymap []
  {:key-left move-left
   :key-right move-right})

(defn setup [world]
  (swap! player (fn [ignore pos] (create-player pos)) (player-position map-data))
  (jme/node [(labyrinth) @player]))

(defn update [world tpf])
