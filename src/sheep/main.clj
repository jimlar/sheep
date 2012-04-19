(ns sheep.main
  (:require [jme.core :as jme]
            [sheep.world :as world]))

(defn -main [& args]
  (.start
    (jme/world
      world/setup
      world/update)))
