package com.github.saintedlittle.data

import kotlinx.serialization.Serializable

@Serializable
data class MovementPoint(
    val x: Double,
    val y: Double,
    val z: Double,
    val timestamp: Long
) {

    fun toLocation(world: org.bukkit.World): org.bukkit.Location {
        return org.bukkit.Location(world, x, y, z)
    }
}
