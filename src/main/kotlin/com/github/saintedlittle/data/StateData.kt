package com.github.saintedlittle.data

import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
data class StateData(
    val isFlying: Boolean,
    val allowFlight: Boolean,
    val gameMode: String,
    val isSleeping: Boolean,
    val isSprinting: Boolean,
    val isDead: Boolean
) {
    companion object {
        fun from(player: Player): StateData {
            return StateData(
                player.isFlying,
                player.allowFlight,
                player.gameMode.name,
                player.isSleeping,
                player.isSprinting,
                player.isDead
            )
        }
    }
}
