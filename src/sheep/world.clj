(ns sheep.world
  (:require [jme.core :as jme]
            [jme.physics :as phys]))

(def map-data ["###################"
               "#          #      #"
               "#          #      #"
               "#          #      #"
               "# ##############  #"
               "# #            #  #"
               "# #      ###   #  #"
               "# ####   # #   #  #"
               "#    #   # #   #  #"
               "#    #     #####  #"
               "#    #            #"
               "#    ###########  #"
               "#              #  #"
               "#    ###########  #"
               "#          p      #"
               "##########   ######"
              ])

(defn positions-of [map-data chr]
  (apply concat
    (map-indexed
      (fn [z row] (filter #(not (nil? %))
                    (map-indexed (fn [x c] (if (= c chr) [x z] nil)) row)))
      map-data)))

(defn wall-positions [map-data]
  (positions-of map-data \#))

(defn player-start-pos [map-data]
  (first (positions-of map-data \p)))

(defn wall-box [x z]
;;  (jme/position (jme/material (jme/box 0.5 0.5 1) "textures/grass/grass.png") x 0 z))
  (jme/position (jme/material (jme/box 0.5 1 0.5)) x 0 z))

(defn labyrinth []
  (jme/node "Labyrinth"
    (conj
      (map #(wall-box (first %) (second %)) (wall-positions map-data))
      (jme/material (jme/position (jme/rotate (jme/geometry "Ground" (jme/quad 50 50)) (- 0 jme/half-pi) 0 0) -25 -1 25) "Textures/Terrain/Rocky/RockyTexture.jpg"))))

(defn create-player
  ([pos] (create-player (first pos) (second pos)))
  ([x z]
    (let [
           player-node (jme/node [(jme/material (jme/sphere 0.3) "Textures/Terrain/Rocky/RockyTexture.jpg")])
           control (phys/character-control player-node 0.05 20 30 30)
           ]
      (phys/location control x 0 z)
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

(defn move-right [world value]
  (let [pos (player-pos)]
    (phys/location (player-control ) (+ (:x pos) (* value player-speed)) (:y pos) (:z pos))))

(defn move-up [world value]
  (let [pos (player-pos)]
    (phys/location (player-control ) (:x pos) (:y pos) (- (:z pos) (* value player-speed )))))

(defn move-down [world value]
  (let [pos (player-pos)]
    (phys/location (player-control ) (:x pos) (:y pos) (+ (:z pos) (* value player-speed )))))

(defn keymap []
  {
    :key-left move-left
    :key-right move-right
    :key-up move-up
    :key-down move-down
  })

(defn setup-camera [world]
  (jme/disable-flyby-cam world)
  (jme/position (jme/rotate (jme/camera-node world (player-node) (player-control)) jme/half-pi jme/half-pi 0) 0 3 0))

(defn setup [world physics-space]
  (let [labyrinth (labyrinth)
        landscape (phys/rigid-body-control (phys/mesh-shape labyrinth))]
    (swap! player (fn [ignore pos] (create-player pos)) (player-start-pos map-data))
    (setup-camera world)
    (jme/add-control labyrinth landscape)
    (.add physics-space (player-control))
    (.add physics-space landscape)
    (jme/node [labyrinth (player-node)])))

(defn update [world tpf])


(defn setup-and-start-world []
  (doto
    (jme/world (keymap) setup update)
    (.start)))
