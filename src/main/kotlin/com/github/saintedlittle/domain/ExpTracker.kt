package com.github.saintedlittle.domain

import org.bukkit.entity.Player
import org.ehcache.Cache
import java.util.*
import kotlin.math.pow

class ExpTracker(
    private val experienceCache: Cache<UUID, Triple<Int, Int, Int>>
) {

    fun updateExperience(player: Player) {
        val playerId = player.uniqueId
        val level = player.level
        val totalExperience = player.totalExperience
        val currentExperience = calculateCurrentExperience(player)

        experienceCache.put(playerId, Triple(level, totalExperience, currentExperience))
    }

    fun getExperience(player: Player): Triple<Int, Int, Int> {
        return experienceCache.get(player.uniqueId) ?: Triple(0, 0, 0)
    }

    fun clearExperience(player: Player) {
        experienceCache.remove(player.uniqueId)
    }

    private fun calculateCurrentExperience(player: Player): Int {
        val level = player.level
        val progressToNextLevel = player.exp
        return experienceAtLevel(level) + (experienceToNextLevel(level) * progressToNextLevel).toInt()
    }

    private fun experienceToNextLevel(level: Int): Int {
        return when {
            level <= 15 -> 2 * level + 7
            level <= 30 -> 5 * level - 38
            else -> 9 * level - 158
        }
    }

    private fun experienceAtLevel(level: Int): Int {
        return when {
            level <= 15 -> (level.toDouble().pow(2.0) + 6 * level).toInt()
            level <= 30 -> (2.5 * level.toDouble().pow(2.0) - 40.5 * level + 360).toInt()
            else -> (4.5 * level.toDouble().pow(2.0) - 162.5 * level + 2220).toInt()
        }
    }

    fun formatExperienceData(player: Player): String {
        val (level, totalExp, currentExp) = getExperience(player)
        return "Player: ${player.name}, Level: $level, Total Exp: $totalExp, Current Exp: $currentExp"
    }
}
