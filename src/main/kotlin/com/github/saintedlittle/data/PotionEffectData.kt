package com.github.saintedlittle.data

import kotlinx.serialization.Serializable

@Serializable
data class PotionEffectData(
    val type: String,
    val amplifier: Int,
    val duration: Int
)
