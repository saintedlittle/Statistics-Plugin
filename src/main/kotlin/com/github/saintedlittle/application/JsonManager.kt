package com.github.saintedlittle.application

import com.github.saintedlittle.data.*
import com.github.saintedlittle.domain.BedTracker
import com.github.saintedlittle.domain.MovementTracker
import com.github.saintedlittle.domain.PlayerTimeTracker
import com.github.saintedlittle.extensions.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.logging.Level

class JsonManager(
    private val timeTracker: PlayerTimeTracker,
    private val bedTracker: BedTracker,
    private val movementTracker: MovementTracker
) {

    private val executorService = Executors.newFixedThreadPool(4)

    fun createPlayerJson(player: Player): String? {
        val future = executorService.submit(Callable {
            try {
                val playerData = collectPlayerData(player)
                clearPlayerData(player)

                playerData.toJson().toString()
            } catch (e: Exception) {
                Bukkit.getLogger().log(Level.SEVERE, "Error processing player JSON", e)
                null
            }
        })

        return try {
            future.get()
        } catch (e: Exception) {
            Bukkit.getLogger().log(Level.SEVERE, "Error getting future result", e)
            null
        }
    }

    private fun collectPlayerData(player: Player): PlayerData {
        val totalTime = timeTracker.getTotalPlayTime(player)
        val beds = bedTracker.getBeds(player)
        val movements = movementTracker.getMovements(player)

        return PlayerData(
            inventory = player.inventory.contents.filterNotNull().map { it.toItemData() },
            armor = player.inventory.armorContents.mapIndexedNotNull { index, item ->
                val slotName = ArmorSlot.entries.getOrNull(index)?.name ?: "unknown"
                val itemData = item?.toItemData() ?: "none"
                slotName to itemData
            }.toMap(),
            statistics = player.collectStatistics(),
            attributes = player.collectAttributes(),
            potionEffects = player.collectPotionEffects(),
            location = player.location.toLocationData(),
            totalTime = totalTime,
            beds = beds.map { it.toLocationData() },
            movements = movements
        )
    }

    private fun clearPlayerData(player: Player) {
        // Очистка данных синхронно
        bedTracker.clearBeds(player)
        movementTracker.clearMovements(player)
        timeTracker.onPlayerExit(player)
    }

    private fun Location.toLocationData(): LocationData {
        return LocationData.from(
            world = world?.name,
            x = x,
            y = y,
            z = z,
            yaw = yaw,
            pitch = pitch
        )
    }

    private fun ItemStack.toItemData(): ItemData {
        return ItemData.from(
            type = type.name,
            minecraftId = type.key.toString(),
            amount = amount,
            displayName = itemMeta?.displayName
        )
    }
}
