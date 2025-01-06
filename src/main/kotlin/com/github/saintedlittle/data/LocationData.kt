package com.github.saintedlittle.data

import com.google.gson.JsonObject

@JvmInline
value class LocationData(
    private val data: Map<String, Any>
) {
    fun toJson(): JsonObject {
        return JsonObject().apply {
            data.forEach { (key, value) ->
                when (value) {
                    is String -> addProperty(key, value)
                    is Double -> addProperty(key, value)
                    is Float -> addProperty(key, value)
                    is Long -> addProperty(key, value)
                    is Int -> addProperty(key, value)
                    is Boolean -> addProperty(key, value)
                    else -> addProperty(key, value.toString())
                }
            }
        }
    }

    companion object {
        fun from(world: String?, x: Double, y: Double, z: Double, yaw: Float, pitch: Float): LocationData {
            return LocationData(
                mapOf(
                    "world" to (world ?: "unknown"),
                    "x" to x,
                    "y" to y,
                    "z" to z,
                    "yaw" to yaw,
                    "pitch" to pitch
                )
            )
        }
    }
}
