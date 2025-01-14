package com.github.saintedlittle.models

import com.github.saintedlittle.application.JsonManager
import com.github.saintedlittle.application.toItemData
import com.github.saintedlittle.data.*
import com.github.saintedlittle.domain.*
import com.github.saintedlittle.extensions.collectAttributes
import com.github.saintedlittle.extensions.collectPotionEffects
import com.github.saintedlittle.extensions.collectStatistics
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

class PlayerWrapper(private val player: OfflinePlayer) {
    private val online: Player?
        get() = (player as? Player)

    val name: String?
        get() = player.name

    val metadata: MetaData?
        get() = online?.let { MetaData.from(it) }

    val inventory: List<ItemData>?
        get() = online?.inventory?.contents
            ?.filterNotNull()
            ?.map { it.toItemData() }

    val armor: Map<String, ItemData>?
        get() = online?.inventory?.armorContents
            ?.mapIndexedNotNull { index, item ->
                ArmorSlot.entries.getOrNull(index)?.name?.let {
                    it to (item?.toItemData() ?: ItemData.empty())
                }
            }?.toMap()

    val statistics: Map<String, Int>
        get() = player.collectStatistics()

    val attributes: Map<String, Double>?
        get() = online?.collectAttributes()

    val potionEffects: List<PotionEffectData>?
        get() = online?.collectPotionEffects()

    val location: LocationData?
        get() = online?.location?.let { LocationData.from(it) }

    fun totalTime(timeTracker: PlayerTimeTracker): Long? =
        online?.let { timeTracker.getTotalPlayTime(it) }

    fun experience(experienceTracker: ExpTracker): Triple<Int, Int, Int>? =
        online?.let { experienceTracker.getExperience(it) }

    fun blockInteractions(blockTracker: BlockTracker): List<BlockInteractionData>? =
        online?.let { blockTracker.getBlockInteractions(it) }

    fun beds(bedTracker: BedTracker): List<LocationData>? =
        online?.let { bedTracker.getBeds(it).map { bed -> LocationData.from(bed) } }

    fun movements(movementTracker: MovementTracker): Map<String, List<MovementPoint>>? =
        online?.let { movementTracker.getMovements(it) }

    fun createPlayerJson(jsonManager: JsonManager): String? =
        online?.let { jsonManager.createPlayerJson(it, readable = false) }
}