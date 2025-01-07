package com.github.saintedlittle.listeners

import com.github.saintedlittle.annotations.AutoRegister
import com.github.saintedlittle.domain.MovementTracker
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.slf4j.Logger
import javax.inject.Inject

@AutoRegister
class MovementListener @Inject constructor(
    private val movementTracker: MovementTracker,
    private val logger: Logger
) : Listener {

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        try {
            val player = event.player
            val to = event.to
            movementTracker.addMovement(player, to)
            logger.debug("Player {} moved to {}.", player.name, to)
        } catch (e: Exception) {
            logger.error("Error handling player movement: {}", e.message, e)
        }
    }

    @EventHandler
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        try {
            val player = event.player
            val to = event.to
            movementTracker.addMovement(player, to)
            logger.debug("Player {} teleported to {}.", player.name, to)
        } catch (e: Exception) {
            logger.error("Error handling player teleport: {}", e.message, e)
        }
    }
}
