package com.github.saintedlittle.data

import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
data class MetaData(
    val uniqueId: String,
    val name: String,
    val server: String
) {
    companion object {
        fun from(player: Player): MetaData {
            return MetaData(
                player.uniqueId.toString(),
                player.name,
                player.server.name
            )
        }
    }
}