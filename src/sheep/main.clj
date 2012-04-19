(ns sheep.main
  (:require [jme.core :as jme]))

(defn -main [& args]
  (jme/warn-logging)
  (jme/view (jme/rotate (jme/node
              [
                (jme/material (jme/box))
                (jme/position (jme/material (jme/box 1 1 1)) 1 1 1)
              ]) 0.3 0.4 0.5 0.6)))
