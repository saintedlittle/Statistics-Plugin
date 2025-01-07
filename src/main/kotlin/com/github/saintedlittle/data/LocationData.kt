package com.github.saintedlittle.data

import kotlinx.serialization.Serializable

@Serializable
data class LocationData(
    val world: String?,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float
) {
    companion object {
        fun from(location: org.bukkit.Location): LocationData {
            return LocationData(
                world = location.world?.name,
                x = location.x,
                y = location.y,
                z = location.z,
                yaw = location.yaw,
                pitch = location.pitch
            )
        }
    }
}
