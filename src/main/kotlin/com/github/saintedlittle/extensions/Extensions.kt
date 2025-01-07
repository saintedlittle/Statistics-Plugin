package com.github.saintedlittle.extensions

import com.github.saintedlittle.data.PotionEffectData
import org.bukkit.Statistic
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect

fun Player.collectStatistics(): Map<String, Int> {
    return Statistic.entries.mapNotNull { statistic ->
        try {
            statistic.name to getStatistic(statistic)
        } catch (e: IllegalArgumentException) {
            null
        }
    }.toMap()
}

fun Player.collectAttributes(): Map<String, Double> {
    return Attribute.entries.mapNotNull { attribute ->
        getAttribute(attribute)?.value?.let { attribute.name to it }
    }.toMap()
}

fun Player.collectPotionEffects(): List<PotionEffectData> {
    return activePotionEffects.map { it.toPotionEffectData() }
}

fun PotionEffect.toPotionEffectData(): PotionEffectData {
    return PotionEffectData(
        type = type.name,
        amplifier = amplifier,
        duration = duration
    )
}