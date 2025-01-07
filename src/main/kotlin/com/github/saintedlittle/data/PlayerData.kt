package com.github.saintedlittle.data

import kotlinx.serialization.Serializable

@Serializable
data class PlayerData(
    val inventory: List<ItemData>,
    val armor: Map<String, ItemData>,
    val statistics: Map<String, Int>,
    val attributes: Map<String, Double>,
    val potionEffects: List<PotionEffectData>,
    val location: LocationData,
    val totalTime: Long,
    val beds: List<LocationData>,
    val movements: Map<String, List<MovementPoint>>
)
