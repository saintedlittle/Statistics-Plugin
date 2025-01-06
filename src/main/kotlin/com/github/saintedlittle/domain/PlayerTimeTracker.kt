package com.github.saintedlittle.domain

import com.github.saintedlittle.utils.javaToKotlinLong
import com.github.saintedlittle.utils.kotlinToJavaLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.bukkit.entity.Player
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.io.File
import java.util.*

class PlayerTimeTracker(private val scope: CoroutineScope, pluginFolder: String) {

    private val cacheManager: CacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .with(CacheManagerBuilder.persistence(File(pluginFolder, "ehcache_player_times")))
        .withCache(
            "playerTimes",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                UUID::class.java,
                java.lang.Long::class.java,
                ResourcePoolsBuilder.heap(1000)
            )
        )
        .withCache(
            "playerSessionStart",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                UUID::class.java,
                java.lang.Long::class.java,
                ResourcePoolsBuilder.heap(1000)
            )
        )
        .build(true)

    private val playerTimes: Cache<UUID, java.lang.Long> =
        cacheManager.getCache("playerTimes", UUID::class.java, java.lang.Long::class.java)
    private val playerSessionStart: Cache<UUID, java.lang.Long> =
        cacheManager.getCache("playerSessionStart", UUID::class.java, java.lang.Long::class.java)

    init {
        scope.launch {
            while (isActive) {
                delay(10_000)
                updateTimes()
            }
        }
    }

    fun onPlayerJoin(player: Player) {
        val playerId = player.uniqueId
        playerSessionStart.put(playerId, kotlinToJavaLong(System.nanoTime()))
    }

    fun onPlayerExit(player: Player) {
        val playerId = player.uniqueId
        val sessionStart = playerSessionStart.get(playerId)
        if (sessionStart != null) {
            val sessionTime = System.nanoTime() - javaToKotlinLong(sessionStart)!!
            val totalPlayTime = javaToKotlinLong(playerTimes.get(playerId)) ?: 0L
            playerTimes.put(playerId, kotlinToJavaLong(totalPlayTime + sessionTime))
            playerSessionStart.remove(playerId)
        }
    }

    private fun updateTimes() {
        playerSessionStart.forEach { entry ->
            val playerId = entry.key
            val sessionStart = javaToKotlinLong(entry.value)
            if (sessionStart != null) {
                val sessionTime = System.nanoTime() - sessionStart
                val totalPlayTime = javaToKotlinLong(playerTimes.get(playerId)) ?: 0L
                playerTimes.put(playerId, kotlinToJavaLong(totalPlayTime + sessionTime))
                playerSessionStart.put(playerId, kotlinToJavaLong(System.nanoTime()))
            }
        }
    }

    fun getTotalPlayTime(player: Player): Long {
        val playerId = player.uniqueId
        val totalPlayTime = javaToKotlinLong(playerTimes.get(playerId)) ?: 0L
        val currentSessionTime = playerSessionStart.get(playerId)?.let {
            System.nanoTime() - javaToKotlinLong(it)!!
        } ?: 0L
        return totalPlayTime + currentSessionTime
    }

    fun close() {
        cacheManager.close()
    }
}
