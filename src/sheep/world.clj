(ns sheep.world
  (:require [jme.core :as jme]))

(defn setup [world]
  (jme/rotate (jme/node
                [
                  (jme/material (jme/box))
                  (jme/position (jme/material (jme/box 1 1 1)) 1 1 1)
                  ]) 0.3 0.4 0.5 0.6))

(defn update [world tpf])
