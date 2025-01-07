package com.github.saintedlittle.data

import kotlinx.serialization.Serializable

@Serializable
data class ItemData(
    val type: String,
    val minecraftId: String,
    val amount: Int,
    val displayName: String? = null
) {
    companion object {
        fun empty(): ItemData = ItemData(
            type = "none",
            minecraftId = "none",
            amount = 0,
            displayName = "none"
        )
    }
}
