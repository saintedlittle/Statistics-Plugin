package com.github.saintedlittle.listeners

import com.github.saintedlittle.annotations.AutoRegister
import com.github.saintedlittle.domain.BedTracker
import com.github.saintedlittle.domain.BlockTracker
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.slf4j.Logger
import javax.inject.Inject

@AutoRegister
class BlockListener @Inject constructor(
    private val blockTracker: BlockTracker,
    private val bedTracker: BedTracker,
    private val logger: Logger
) : Listener {

    companion object {
        private val bedMaterials: Set<Material> by lazy {
            Material.entries.filter { it.name.contains("BED", ignoreCase = true) }.toSet()
        }
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        try {
            val player = event.player
            blockTracker.addBlockPlace(player, event.blockReplacedState, event.block.state)
            if (event.block.type in bedMaterials)
                bedTracker.addBed(player, event.block.location)
            logger.debug("Player {} placed a block at {}.", player.name, event.block.location)
        } catch (e: Exception) {
            logger.error("Error processing block placement: {}", e.message, e)
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        try {
            val player = event.player
            blockTracker.addBlockBreak(player, event.block.state)
            logger.debug("Player {} broke the block at {}.", player.name, event.block.location)
        } catch (e: Exception) {
            logger.error("Error processing block breaking: {}", e.message, e)
        }
    }
}
