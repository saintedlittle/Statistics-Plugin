package com.github.saintedlittle.listeners

import com.github.saintedlittle.annotations.AutoRegister
import com.github.saintedlittle.application.JsonManager
import com.github.saintedlittle.domain.ExpTracker
import com.github.saintedlittle.domain.PlayerTimeTracker
import com.github.saintedlittle.messaging.KafkaProducerService
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
    private val kafkaProducerService: KafkaProducerService,
    private val jsonManager: JsonManager,
    private val expTracker: ExpTracker,
    private val scope: CoroutineScope,
    private val logger: Logger
) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        try {
            tracker.onPlayerJoin(player)
            expTracker.updateExperience(player)
            kafkaProducerService.sendPlayerLogin(player.uniqueId.toString(), System.currentTimeMillis().toString())
            logger.info("Player ${player.name} joined the server.")
        } catch (e: Exception) {
            logger.error("Error during PlayerJoinEvent for ${player.name}: ${e.message}", e)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        scope.launch {
            try {
                val playerJson = jsonManager.createPlayerJson(player)
                kafkaProducerService.sendPlayerLogout(player.uniqueId.toString(), playerJson)
                logger.debug("Generated JSON for player ${player.name}: $playerJson")
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
