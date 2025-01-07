package com.github.saintedlittle.listeners

import com.github.saintedlittle.annotations.AutoRegister
import com.github.saintedlittle.domain.ExpTracker
import com.google.inject.Inject
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerExpChangeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.slf4j.Logger

@AutoRegister
class PlayerExpListener @Inject constructor(
    private val expTracker: ExpTracker,
    private val logger: Logger
) {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        expTracker.update(event.player)
    }

    @EventHandler
    fun onPlayerExpChange(event: PlayerExpChangeEvent) {
        try {
            val player = event.player
            expTracker.update(player)
            logger.debug("Player {} received {} experience.", player.name, event.amount)
        } catch (e: Exception) {
            logger.error("Error during PlayerExpChangeEvent for ${event.player.name}: ${e.message}", e)
        }
    }
}