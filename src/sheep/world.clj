(ns sheep.world
  (:require [jme.core :as jme]))

(def map-data ["#######"
               "# #   #"
               "# # # #"
               "#   # #"
               "#######"])

(defn wall-positions [map-data]
  (apply concat
    (map-indexed
      (fn [y row] (filter #(not (nil? %))
                    (map-indexed (fn [x chr] (if (= chr \#) [x y] nil)) row)))
      map-data)))

(defn wall-box [x y]
  (jme/position (jme/material (jme/box 0.5 0.5 0.5)) x y 0))

(defn labyrinth []
  (jme/node "Labyrinth" (map #(wall-box (first %) (second %)) (wall-positions map-data))))

(defn player []
  (jme/scale (jme/load-model "Models/Ninja/Ninja.mesh.xml") 0.05 0.05 0.05))

(defn setup [world]
  (jme/node [(labyrinth) (player)]))

(defn update [world tpf])
