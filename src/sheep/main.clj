(ns sheep.main
  (:require [jme.core :as jme]
            [sheep.world :as world]))

(defn -main [& args]
  (.start
    (jme/world
      (world/keymap)
      world/setup
      world/update)))
