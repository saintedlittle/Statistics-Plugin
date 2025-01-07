package com.github.saintedlittle.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.ehcache.Cache
import java.util.*

class BedTracker(
    private val playerBeds: Cache<UUID, String>
) {

    private val json = Json

    fun addBed(player: Player, location: Location) {
        val playerId = player.uniqueId
        val beds = getBeds(player).map { it.toSerializable() }.toMutableList()
        beds.add(location.toSerializable())
        playerBeds.put(playerId, json.encodeToString(beds))
    }

    fun getBeds(player: Player): List<Location> {
        val playerId = player.uniqueId
        val bedsJson = playerBeds.get(playerId) ?: return emptyList()
        val serializedBeds = json.decodeFromString<List<SerializableLocation>>(bedsJson)
        return serializedBeds.map { it.toLocation() }
    }

    fun clearBeds(player: Player) {
        playerBeds.remove(player.uniqueId)
    }

    @Serializable
    data class SerializableLocation(
        val worldName: String?,
        val x: Double,
        val y: Double,
        val z: Double,
        val yaw: Float,
        val pitch: Float
    ) {
        fun toLocation(): Location {
            val world = if (worldName != null) Bukkit.getWorld(worldName) else null
            return Location(world, x, y, z, yaw, pitch)
        }
    }

    private fun Location.toSerializable(): SerializableLocation {
        return SerializableLocation(
            world?.name,
            x,
            y,
            z,
            yaw,
            pitch
        )
    }
}
