package com.google.ar.core.vibaplatform.kotlin.viba

// -------------------- fabrizio ---------------------------------
class CollisionPointsCoordinates {

    public var distanceThreshold: Float = 1500f
    public val pixelRadiusThreshold: Int = 5

    companion object {
        
        private val pointCoordinates = mutableMapOf<String, Pair<Float, Float>>()

        init {
            // head points
            pointCoordinates["h0"] = Pair(10/24.0f, 1/8.0f)
            pointCoordinates["h1"] = Pair(12/24.0f, 1/8.0f)
            pointCoordinates["h2"] = Pair(14/24.0f, 1/8.0f)
            // chest left points
            pointCoordinates["cl0"] = Pair(6/24.0f, 2/8.0f)
            pointCoordinates["cl1"] = Pair(12/24.0f, 2/8.0f)
            pointCoordinates["cl2"] = Pair(9/24.0f, 3/8.0f)
            pointCoordinates["cl3"] = Pair(6/24.0f, 4/8.0f)
            pointCoordinates["cl4"] = Pair(12/24.0f, 4/8.0f)
            pointCoordinates["cl5"] = Pair(9/24.0f, 5/8.0f)
            pointCoordinates["cl6"] = Pair(6/24.0f, 6/8.0f)
            pointCoordinates["cl7"] = Pair(12/24.0f, 6/8.0f)
            // chest right points
            pointCoordinates["cr0"] = Pair(12/24.0f, 2/8.0f)
            pointCoordinates["cr1"] = Pair(18/24.0f, 2/8.0f)
            pointCoordinates["cr2"] = Pair(15/24.0f, 3/8.0f)
            pointCoordinates["cr3"] = Pair(12/24.0f, 4/8.0f)
            pointCoordinates["cr4"] = Pair(18/24.0f, 4/8.0f)
            pointCoordinates["cr5"] = Pair(15/24.0f, 5/8.0f)
            pointCoordinates["cr6"] = Pair(12/24.0f, 6/8.0f)
            pointCoordinates["cr7"] = Pair(18/24.0f, 6/8.0f)
            // leg left
            pointCoordinates["ll0"] = Pair(6/24.0f, 7/8.0f)
            pointCoordinates["ll1"] = Pair(12/24.0f, 7/8.0f)
            // leg right
            pointCoordinates["lr0"] = Pair(12/24.0f, 7/8.0f)
            pointCoordinates["lr1"] = Pair(18/24.0f, 7/8.0f)
        }
        
        private val bodyParts = mutableMapOf<String, Array<String>>()
        
        init{
            bodyParts["head"] = arrayOf("h0", "h1", "h2")
            bodyParts["chest_left"] = arrayOf("cl0", "cl1", "cl2", "cl3", "cl4", "cl5", "cl6", "cl7")
            bodyParts["chest_right"] = arrayOf("cr0", "cr1", "cr2", "cr3", "cr4", "cr5", "cr6", "cr7")
            bodyParts["leg_left"] = arrayOf("ll0", "ll1")
            bodyParts["leg_right"] = arrayOf("lr0", "lr1")
        }
    }

    /* ---------------- Biagio ---------------- */
    fun getKeys(): MutableSet<String> {
        return pointCoordinates.keys
    }
    /* ---------------------------------------- */

    fun getCoordinatesByPointId(id: String): Pair<Float, Float>? {
        return pointCoordinates[id]
    }

    fun getBodyPartByPointId(id: String): String?{
        for ((key, values) in bodyParts) {
            if (id in values) {
                return key
            }
        }
        return null
    }

    fun getPointCoordinatesMap():MutableMap<String, Pair<Float, Float>> {
        return pointCoordinates
    }

    fun getBodyPartsMap():MutableMap<String, Array<String>> {
        return bodyParts
    }
}


// -------------------- fabrizio -------------------------------