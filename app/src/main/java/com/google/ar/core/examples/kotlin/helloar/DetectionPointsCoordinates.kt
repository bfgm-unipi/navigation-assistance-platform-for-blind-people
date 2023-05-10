package com.google.ar.core.examples.kotlin.helloar

class DetectionPointsCoordinates {

    companion object {
        private val coordinates = mutableMapOf<String, Pair<Float, Float>>()

        init {
            coordinates["h0"] = Pair(1/8.0f, 2/8.0f)
            coordinates["h1"] = Pair(1/8.0f, 4/8.0f)
            coordinates["h2"] = Pair(1/8.0f, 6/8.0f)
            coordinates["cl0"] = Pair(2/8.0f, 1/8.0f)
            coordinates["cl1"] = Pair(2/8.0f, 3/8.0f)
            coordinates["cl2"] = Pair(3/8.0f, 2/8.0f)
            coordinates["cl3"] = Pair(4/8.0f, 1/8.0f)
            coordinates["cl4"] = Pair(4/8.0f, 3/8.0f)
            coordinates["cl5"] = Pair(5/8.0f, 2/8.0f)
            coordinates["cl6"] = Pair(6/8.0f, 1/8.0f)
            coordinates["cl7"] = Pair(6/8.0f, 3/8.0f)
            coordinates["cr0"] = Pair(2/8.0f, 5/8.0f)
            coordinates["cr1"] = Pair(2/8.0f, 7/8.0f)
            coordinates["cr2"] = Pair(3/8.0f, 6/8.0f)
            coordinates["cr3"] = Pair(4/8.0f, 5/8.0f)
            coordinates["cr4"] = Pair(4/8.0f, 7/8.0f)
            coordinates["cr5"] = Pair(5/8.0f, 6/8.0f)
            coordinates["cr6"] = Pair(6/8.0f, 5/8.0f)
            coordinates["cr7"] = Pair(6/8.0f, 7/8.0f)
            coordinates["ll0"] = Pair(7/8.0f, 1/8.0f)
            coordinates["ll1"] = Pair(7/8.0f, 3/8.0f)
            coordinates["lr0"] = Pair(7/8.0f, 5/8.0f)
            coordinates["lr1"] = Pair(7/8.0f, 7/8.0f)
        }

        fun getCoordinatesById(name: String): Pair<Float, Float>? {
            return coordinates[name]
        }
    }
}