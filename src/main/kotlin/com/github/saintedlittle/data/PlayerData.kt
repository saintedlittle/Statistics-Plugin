package com.github.saintedlittle.data

import kotlinx.serialization.Serializable

@Serializable
data class PlayerData(
    val metaData: MetaData,
    val inventory: List<ItemData>,
    val armor: Map<String, ItemData>,
    val statistics: Map<String, Int>,
    val attributes: Map<String, Double>,
    val potionEffects: List<PotionEffectData>,
    val location: LocationData,
    val totalTime: Long,
    val level: Int,
    val totalExp: Int,
    val currentExp: Int,
    val blockInteractions: List<BlockInteractionData>,
    val beds: List<LocationData>,
    val movements: Map<String, List<MovementPoint>>
)
