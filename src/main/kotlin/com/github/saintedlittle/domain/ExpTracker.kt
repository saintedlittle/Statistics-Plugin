package com.github.saintedlittle.domain

import org.bukkit.entity.Player
import org.ehcache.Cache
import java.util.*
import kotlin.math.pow

class ExpTracker(
    private val playerExp: Cache<UUID, Triple<Int, Int, Int>>
) {
    fun update(player: Player) {
        val playerId = player.uniqueId

        val level = player.level
        val totalExp = player.totalExperience
        val currentExp = getCurrentExp(player)

        playerExp.put(playerId, Triple(level, totalExp, currentExp))
    }

    fun getExp(player: Player): Triple<Int, Int, Int> {
        val playerId = player.uniqueId
        return playerExp.get(playerId) ?: Triple(0, 0, 0)
    }

    private fun getCurrentExp(player: Player): Int {
        val level = player.level
        return getExpAtLevel(level) + Math.round(getExpToLevelUp(level) * player.exp)
    }

    private fun getExpToLevelUp(level: Int): Int {
        return if (level <= 15) {
            2 * level + 7
        } else if (level <= 30) {
            5 * level - 38
        } else {
            9 * level - 158
        }
    }

    private fun getExpAtLevel(level: Int): Int {
        return if (level <= 16) {
            (level.toDouble().pow(2.0) + 6 * level).toInt()
        } else if (level <= 31) {
            (2.5 * level.toDouble().pow(2.0) - 40.5 * level + 360.0).toInt()
        } else {
            (4.5 * level.toDouble().pow(2.0) - 162.5 * level + 2220.0).toInt()
        }
    }
}