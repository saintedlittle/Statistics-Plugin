package com.github.saintedlittle.domain

import com.github.saintedlittle.application.ConfigManager
import com.github.saintedlittle.data.MovementPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Location
import org.bukkit.entity.Player
import org.ehcache.Cache
import java.util.*

class MovementTracker(
    private val scope: CoroutineScope,
    private val playerMovements: Cache<UUID, String>,
    private val configManager: ConfigManager
) {

    private val json = Json

    fun addMovement(player: Player, location: Location) {
        scope.launch {
            val worldName = location.world?.name ?: return@launch
            val playerId = player.uniqueId

            val movementsByWorld = getMovements(player).toMutableMap()
            val movementsInWorld = movementsByWorld.getOrPut(worldName) { mutableListOf() }.toMutableList()

            val lastPoint = movementsInWorld.lastOrNull()
            val minDistance = configManager.config.getDouble("minDistanceBetweenMovements", DEFAULT_MIN_DISTANCE)

            if (lastPoint == null || location.distance(lastPoint.toLocation(location.world!!)) >= minDistance) {
                movementsInWorld.add(MovementPoint(location.x, location.y, location.z, System.currentTimeMillis()))
                movementsByWorld[worldName] = movementsInWorld
                playerMovements.put(playerId, json.encodeToString(movementsByWorld))
            }
        }
    }

    fun getMovements(player: Player): Map<String, List<MovementPoint>> {
        val playerId = player.uniqueId
        val movementsJson = playerMovements.get(playerId) ?: "{}"
        return json.decodeFromString(movementsJson)
    }

    fun clearMovements(player: Player) {
        scope.launch {
            playerMovements.remove(player.uniqueId)
        }
    }

    companion object {
        private const val DEFAULT_MIN_DISTANCE = 2.0
    }
}
