package com.github.saintedlittle.domain

import com.github.saintedlittle.application.ConfigManager
import com.github.saintedlittle.data.MovementPoint
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.bukkit.Location
import org.bukkit.entity.Player
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.config.units.MemoryUnit
import java.io.File
import java.util.*

class MovementTracker(
    private val scope: CoroutineScope,
    pluginFolder: String,
    private val configManager: ConfigManager
) {

    private val cacheManager: CacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .with(CacheManagerBuilder.persistence(File(pluginFolder, "ehcache_movements")))
        .withCache(
            "playerMovements",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                UUID::class.java,
                String::class.java,
                ResourcePoolsBuilder.heap(70000)
                    .disk(5, MemoryUnit.GB, true)
            )
        )
        .build(true)

    private val playerMovements: Cache<UUID, String> =
        cacheManager.getCache("playerMovements", UUID::class.java, String::class.java)

    fun addMovement(player: Player, location: Location) {
        scope.launch {
            val worldName = location.world?.name ?: return@launch
            val playerId = player.uniqueId
            val movementsJson = playerMovements.get(playerId) ?: "{}"
            val movementsByWorld = deserializeMovements(movementsJson)

            val movementsInWorld = movementsByWorld.computeIfAbsent(worldName) { mutableListOf() }
            val lastPoint = movementsInWorld.lastOrNull()

            val minDistance = configManager.get("minDistanceBetweenMovements")?.toDoubleOrNull()
                ?: DEFAULT_MIN_DISTANCE

            if (lastPoint == null || location.distance(lastPoint.toLocation(location.world!!)) >= minDistance) {
                movementsInWorld.add(MovementPoint(location.x, location.y, location.z, System.currentTimeMillis()))
                playerMovements.put(playerId, serializeMovements(movementsByWorld))
            }
        }
    }

    fun getMovements(player: Player): Map<String, List<MovementPoint>> {
        val playerId = player.uniqueId
        val movementsJson = playerMovements.get(playerId) ?: return emptyMap()
        return deserializeMovements(movementsJson)
    }

    fun clearMovements(player: Player) {
        scope.launch {
            playerMovements.remove(player.uniqueId)
        }
    }

    private fun serializeMovements(movements: Map<String, List<MovementPoint>>): String {
        return movements.entries.joinToString(prefix = "{", postfix = "}") { (world, points) ->
            """"$world":[${points.joinToString(",") { it.toJsonObject() }}]"""
        }
    }

    private fun deserializeMovements(json: String): MutableMap<String, MutableList<MovementPoint>> {
        val result = mutableMapOf<String, MutableList<MovementPoint>>()
        val jsonObject = JsonParser.parseString(json).asJsonObject
        jsonObject.entrySet().forEach { (world, pointsJson) ->
            val points = pointsJson.asJsonArray.map { pointJson ->
                pointJson.asJsonObject.let {
                    MovementPoint(
                        it["x"].asDouble,
                        it["y"].asDouble,
                        it["z"].asDouble,
                        it["timestamp"].asLong
                    )
                }
            }.toMutableList()
            result[world] = points
        }
        return result
    }

    fun close() {
        cacheManager.close()
    }

    companion object {
        private const val DEFAULT_MIN_DISTANCE = 2.0
    }
}