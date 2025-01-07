package com.github.saintedlittle.listeners

import com.github.saintedlittle.annotations.AutoRegister
import com.github.saintedlittle.domain.BedTracker
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.slf4j.Logger
import javax.inject.Inject

@AutoRegister
class BlockListener @Inject constructor(
    private val bedTracker: BedTracker,
    private val logger: Logger
) : Listener {

    companion object {
        private val bedMaterials: Set<Material> = Material.entries
            .filter { it.name.contains("BED", ignoreCase = true) }
            .toSet()
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (event.block.type in bedMaterials) {
            bedTracker.addBed(event.player, event.block.location)
            logger.info("Player ${event.player.name} placed a bed at ${event.block.location}.")
        }
    }
}
