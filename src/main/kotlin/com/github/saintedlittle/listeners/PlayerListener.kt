package com.github.saintedlittle.listeners

import com.github.saintedlittle.application.JsonManager
import com.github.saintedlittle.domain.PlayerTimeTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener(
    private val tracker: PlayerTimeTracker,
    private val jsonManager: JsonManager,
    private val scope: CoroutineScope
) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        tracker.onPlayerJoin(event.player)
        Bukkit.getLogger().info("${event.player.name} joined the server.")
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        scope.launch {
            try {
                val playerJson = jsonManager.createPlayerJson(player)
                Bukkit.getLogger().info("Player JSON: $playerJson")
            } catch (e: Exception) {
                Bukkit.getLogger().severe("Failed to process player JSON for ${player.name}: ${e.message}")
            }
        }
    }
}