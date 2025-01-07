package com.github.saintedlittle.data

import kotlinx.serialization.Serializable

@Serializable
data class MovementPoint(
    val x: Double,
    val y: Double,
    val z: Double,
    val timestamp: Long
) {
    fun toJsonObject(): String {
        return """{"x":$x,"y":$y,"z":$z,"timestamp":$timestamp}"""
    }

    fun toLocation(world: org.bukkit.World): org.bukkit.Location {
        return org.bukkit.Location(world, x, y, z)
    }
}
