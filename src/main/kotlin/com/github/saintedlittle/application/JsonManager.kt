package com.github.saintedlittle.application


import com.github.saintedlittle.data.ArmorSlot
import com.github.saintedlittle.data.LocationData
import com.github.saintedlittle.data.PlayerData
import com.github.saintedlittle.domain.BedTracker
import com.github.saintedlittle.domain.MovementTracker
import com.github.saintedlittle.domain.PlayerTimeTracker
import com.github.saintedlittle.extensions.collectAttributes
import com.github.saintedlittle.extensions.collectPotionEffects
import com.github.saintedlittle.extensions.collectStatistics
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.entity.Player
import com.github.saintedlittle.data.ItemData
import com.github.saintedlittle.domain.ExpTracker
import com.google.inject.Inject
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.inventory.ItemStack

fun ItemStack.toItemData(): ItemData {
    val serializer = PlainTextComponentSerializer.plainText()
    val displayName = itemMeta?.displayName()?.let { serializer.serialize(it) } ?: "unknown"

    return ItemData(
        type = type.name,
        minecraftId = type.key.toString(),
        amount = amount,
        displayName = displayName
    )
}

object JsonUtil {
    val json = Json { prettyPrint = true }

    inline fun <reified T> toJson(obj: T): String {
        return json.encodeToString(obj)
    }

    inline fun <reified T> fromJson(jsonString: String): T {
        return json.decodeFromString(jsonString)
    }
}


class JsonManager @Inject constructor(
    private val timeTracker: PlayerTimeTracker,
    private val bedTracker: BedTracker,
    private val movementTracker: MovementTracker,
    private val expTracker: ExpTracker
) {

    fun createPlayerJson(player: Player): String {
        val playerData = collectPlayerData(player)
        clearPlayerData(player)
        return JsonUtil.toJson(playerData)
    }

    private fun collectPlayerData(player: Player): PlayerData {
        val totalTime = timeTracker.getTotalPlayTime(player)
        val beds = bedTracker.getBeds(player).map { LocationData.from(it) }
        val movements = movementTracker.getMovements(player)
        val exp = expTracker.getExperience(player)

        return PlayerData(
            inventory = player.inventory.contents.filterNotNull().map { it.toItemData() },
            armor = player.inventory.armorContents.mapIndexedNotNull { index, item ->
                ArmorSlot.entries.getOrNull(index)?.name?.let { it to (item?.toItemData() ?: ItemData.empty()) }
            }.toMap(),
            statistics = player.collectStatistics(),
            attributes = player.collectAttributes(),
            potionEffects = player.collectPotionEffects(),
            location = LocationData.from(player.location),
            totalTime = totalTime,
            beds = beds,
            level = exp.first,
            totalExp = exp.second,
            currentExp = exp.third,
            movements = movements
        )
    }

    private fun clearPlayerData(player: Player) {
        expTracker.clearExperience(player)
        bedTracker.clearBeds(player)
        movementTracker.clearMovements(player)
        timeTracker.onPlayerExit(player)
    }
}

