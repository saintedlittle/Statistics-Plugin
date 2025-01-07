package com.github.saintedlittle.listeners

import com.github.saintedlittle.annotations.AutoRegister
import com.github.saintedlittle.application.JsonManager
import com.github.saintedlittle.domain.ExpTracker
import com.github.saintedlittle.domain.PlayerTimeTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerExpChangeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.slf4j.Logger
import javax.inject.Inject

@AutoRegister
class PlayerEventListener @Inject constructor(
    private val tracker: PlayerTimeTracker,
    private val jsonManager: JsonManager,
    private val expTracker: ExpTracker,
    private val scope: CoroutineScope,
    private val logger: Logger
) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        try {
            tracker.onPlayerJoin(event.player)
            expTracker.updateExperience(event.player)
            logger.info("Player ${event.player.name} joined the server.")
        } catch (e: Exception) {
            logger.error("Error during PlayerJoinEvent for ${event.player.name}: ${e.message}", e)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        scope.launch {
            try {
                val playerJson = jsonManager.createPlayerJson(player)
                logger.info("Generated JSON for player ${player.name}: $playerJson")
            } catch (e: Exception) {
                logger.error("Error during PlayerQuitEvent for ${player.name}: ${e.message}", e)
            } finally {
                try {
                    tracker.onPlayerExit(player)
                } catch (e: Exception) {
                    logger.error("Error saving time tracker data for ${player.name}: ${e.message}", e)
                }
            }
        }
    }

    @EventHandler
    fun onPlayerExpChange(event: PlayerExpChangeEvent) {
        try {
            val player = event.player
            expTracker.updateExperience(player)
            logger.debug("Player {} received {} experience.", player.name, event.amount)
        } catch (e: Exception) {
            logger.error("Error during PlayerExpChangeEvent for ${event.player.name}: ${e.message}", e)
        }
    }
}
