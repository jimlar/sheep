(ns sheep.main
  (:require [jme.core :as jme]
            [sheep.world :as world]))

(defn -main [& args]
  (world/setup-and-start-world))
