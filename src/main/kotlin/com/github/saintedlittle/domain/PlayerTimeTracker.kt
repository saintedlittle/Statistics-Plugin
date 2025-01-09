package com.github.saintedlittle.domain

import com.github.saintedlittle.utils.toJavaLong
import com.github.saintedlittle.utils.toKotlinLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.bukkit.entity.Player
import org.ehcache.Cache
import java.util.*

class PlayerTimeTracker(
    scope: CoroutineScope,
    private val playerTimes: Cache<UUID, java.lang.Long>,
    private val playerSessionStart: Cache<UUID, java.lang.Long>
) {

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
        playerSessionStart.put(playerId, System.nanoTime().toJavaLong())
    }

    fun onPlayerExit(player: Player) {
        val playerId = player.uniqueId
        val sessionStart = playerSessionStart.get(playerId)
        if (sessionStart != null) {
            val sessionTime = System.nanoTime() - sessionStart.toKotlinLong()
            val totalPlayTime = playerTimes.get(playerId).let { it?.toKotlinLong() } ?: 0L
            playerTimes.put(playerId, (totalPlayTime + sessionTime).toJavaLong())
            playerSessionStart.remove(playerId)
        }
    }

    private fun updateTimes() {
        playerSessionStart.forEach { entry ->
            val playerId = entry.key
            val sessionStart = (entry.value).toKotlinLong()
            val sessionTime = System.nanoTime() - sessionStart
            val totalPlayTime = playerTimes.get(playerId).let { it?.toKotlinLong() } ?: 0L
            playerTimes.put(playerId, (totalPlayTime + sessionTime).toJavaLong())
            playerSessionStart.put(playerId, (System.nanoTime()).toJavaLong())
        }
    }

    fun getTotalPlayTime(player: Player): Long {
        val playerId = player.uniqueId
        val totalPlayTime = playerTimes.get(playerId).let { it?.toKotlinLong() } ?: 0L
        val currentSessionTime = playerSessionStart.get(playerId)?.let {
            System.nanoTime() - it.toKotlinLong()
        } ?: 0L
        return totalPlayTime + currentSessionTime
    }

}
