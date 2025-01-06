package com.github.saintedlittle.listeners

import com.github.saintedlittle.domain.MovementTracker
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class MovementListener(private val movementTracker: MovementTracker) : Listener {

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val to = event.to

        movementTracker.addMovement(player, to)
    }
}
