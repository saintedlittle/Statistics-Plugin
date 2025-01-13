package com.github.saintedlittle.data

import kotlinx.serialization.Serializable

@Serializable
data class BlockInteractionData(
    val type: String,
    val location: LocationData,
    val blockBefore: String,
    val blockAfter: String
)