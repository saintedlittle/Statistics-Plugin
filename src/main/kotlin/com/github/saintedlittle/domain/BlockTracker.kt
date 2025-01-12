package com.github.saintedlittle.domain

import com.github.saintedlittle.data.BlockInteractionData
import com.github.saintedlittle.data.LocationData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Material
import org.bukkit.block.BlockState
import org.bukkit.entity.Player
import org.ehcache.Cache
import java.util.*

class BlockTracker(
    private val scope: CoroutineScope,
    private val playerBlockInteractions: Cache<UUID, String>
) {

    private val json = Json

    fun addBlockPlace(player: Player, blockBeforeState: BlockState, blockState: BlockState) {
        scope.launch {
            val interactionType = InteractionType.BLOCK_PLACE

            val interaction = BlockInteractionData(
                interactionType.id,
                LocationData.from(blockState.location),
                blockBeforeState.blockData.asString,
                blockState.blockData.asString
            )

            addInteraction(player, interaction)
        }
    }

    fun addBlockBreak(player: Player, blockState: BlockState) {
        scope.launch {
            val interactionType = InteractionType.BLOCK_BREAK

            val interaction = BlockInteractionData(
                interactionType.id,
                LocationData.from(blockState.location),
                blockState.blockData.asString,
                Material.AIR.createBlockData().asString
            )

            addInteraction(player, interaction)
        }
    }

    fun getBlockInteractions(player: Player): List<BlockInteractionData> {
        val playerId = player.uniqueId
        val interactions = playerBlockInteractions[playerId] ?: "[]"
        return json.decodeFromString(interactions)
    }

    fun clearBlockInteractions(player: Player) {
        scope.launch {
            playerBlockInteractions.remove(player.uniqueId)
        }
    }

    private fun addInteraction(player: Player, interaction: BlockInteractionData) {
        val playerId = player.uniqueId
        val playerInteractions = getBlockInteractions(player).toMutableList()
        playerInteractions.add(interaction)

        playerBlockInteractions.put(playerId, json.encodeToString(playerInteractions))
    }

    private enum class InteractionType(val id: String) {
        BLOCK_PLACE("block_place"),
        BLOCK_BREAK("block_break")
    }
}