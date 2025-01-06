package com.github.saintedlittle.listeners

import com.github.saintedlittle.domain.BedTracker
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

class BlockListener(private val bedTracker: BedTracker) : Listener {

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (event.block.type == Material.RED_BED || event.block.type == Material.BLUE_BED) {
            bedTracker.addBed(event.player, event.block.location)
            Bukkit.getLogger().info("${event.player.name} placed a bed at ${event.block.location}.")
        }
    }
}