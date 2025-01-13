package com.github.saintedlittle.data

import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
data class MetaData(
    val uniqueId: String,
    val name: String,
    val stateData: StateData
) {
    companion object {
        fun from(player: Player): MetaData {
            return MetaData(
                player.uniqueId.toString(),
                player.name,
                StateData.from(player)
            )
        }
    }
}