package com.github.saintedlittle.placeholderapi

import com.github.saintedlittle.application.ConfigManager
import com.github.saintedlittle.application.toItemData
import com.github.saintedlittle.domain.BlockTracker
import com.github.saintedlittle.domain.ExpTracker
import com.github.saintedlittle.domain.MovementTracker
import com.github.saintedlittle.extensions.splitFromEnd
import com.github.saintedlittle.extensions.toPotionEffectData
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.Statistic
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Damageable
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.slf4j.Logger

class Placeholder(
    private val configManager: ConfigManager,
    private val expTracker: ExpTracker,
    private val blockTracker: BlockTracker,
    private val movementTracker: MovementTracker,
    private val logger: Logger
) : PlaceholderExpansion() {
    override fun getIdentifier(): String = "statistics"

    override fun getAuthor(): String = "StatisticsPlugin"

    override fun getVersion(): String = "1.0"

    override fun register(): Boolean {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            logger.warn("Failed to register placeholder: plugin PlaceholderAPI is disabled!")
            return false
        } else if (configManager.placeholderConfig["enabled"] != "true") {
            logger.warn("Placeholders are disabled!")
            return false
        }

        return super.register()
    }

    private enum class PlaceholderType(val key: String) {
        NICKNAME("nickname"),
        STATISTIC("statistic"),
        ATTRIBUTE("attribute"),
        INVENTORY_ITEM("inventory_item"),
        POTION_EFFECTS("potion_effects"),
        LOCATION("location"),
        EXP("exp"),
        BLOCK_INTERACTIONS("block_interactions"),
        MOVEMENTS("movements"),
        STATISTIC_TOP("statistic_top");

        companion object {
            fun byKey(string: String): PlaceholderType? {
                return entries.firstOrNull { it.key == string }
            }
        }
    }

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        val placeholderConfig = configManager.placeholderConfig
        val (key, placeholder) = placeholderConfig.entries.asSequence()
            .mapNotNull { (key, value) ->
                if (key is String && key != "enabled" && value is String) {
                    val placeholder = extractValue(value.removePrefix(name + "_"), params)
                    if (placeholder != null) key to placeholder
                    else null
                } else null
            }.firstOrNull() ?: return null

        return when (PlaceholderType.byKey(key)) {
            PlaceholderType.NICKNAME -> handleNickname(player)
            PlaceholderType.STATISTIC -> handleStatistic(player, placeholder)
            PlaceholderType.ATTRIBUTE -> handleAttribute(player, placeholder)
            PlaceholderType.INVENTORY_ITEM -> handleInventoryItem(player, placeholder)
            PlaceholderType.POTION_EFFECTS -> handlePotionEffects(player, placeholder)
            PlaceholderType.LOCATION -> handleLocation(player, placeholder)
            PlaceholderType.EXP -> handleExperience(player, placeholder)
            PlaceholderType.BLOCK_INTERACTIONS -> handleBlockInteractions(player, placeholder)
            PlaceholderType.MOVEMENTS -> handleMovements(player, placeholder)
            PlaceholderType.STATISTIC_TOP -> handleStatisticTop(placeholder)
            else -> null
        }
    }

    private fun handleNickname(player: Player?): String? = player?.name

    private fun handleStatistic(player: Player?, value: String): String? {
        if (player == null) return null

        val values = value.split(':')
        val statistic = if (values.size >= 2) {
            Statistic.entries.firstOrNull { it.name.equals(values[0], true) }
        } else {
            Statistic.entries.firstOrNull { it.name.equals(value, true) }
        } ?: return null

        return try {
            when (val type = values.getOrNull(1)?.getType()) {
                is EntityType -> player.getStatistic(statistic, type).toString()
                is Material -> player.getStatistic(statistic, type).toString()
                else -> player.getStatistic(statistic).toString()
            }
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private fun handleAttribute(player: Player?, value: String): String? {
        if (player == null) return null

        val attribute = Attribute.entries.firstOrNull { it.name.equals(value, true) } ?: return null
        val attributeInstance = player.getAttribute(attribute) ?: return null

        return attributeInstance.value.toString()
    }

    private fun handleInventoryItem(player: Player?, value: String): String? {
        if (player == null) return null

        val args = value.split('_')
        try {
            val slot = args[0].toInt()
            if (slot >= player.inventory.size) return null
            val undefined = configManager.config.getString("placeholder.undefined", "Undefined")
            val item = player.inventory.getItem(slot) ?: return undefined

            val miniMessage = MiniMessage.miniMessage()

            return if (args.size == 1) {
                item.toItemData().toString()
            } else {
                when (args[1].lowercase()) {
                    "displayname" -> miniMessage.serializeOrNull(item.displayName()) ?: undefined
                    "lore" -> {
                        if (item.lore().isNullOrEmpty()) return undefined
                        else item.lore().orEmpty().mapNotNull { miniMessage.serializeOrNull(it) }.joinToString()
                    }
                    "type" -> item.type.name
                    "amount" -> item.amount.toString()
                    "maxstacksize" -> item.maxStackSize.toString()
                    "itemflags" -> {
                        if (item.itemFlags.isEmpty()) return undefined
                        else item.itemFlags.mapNotNull { it.name }.joinToString()
                    }
                    "durability" -> {
                        if (item is Damageable) (item as Damageable).health.toString()
                        else undefined
                    }
                    else -> null
                }
            }
        } catch (e: NumberFormatException) {
            return null
        }
    }

    private fun handlePotionEffects(player: Player?, value: String): String? {
        if (player == null) return null

        val args = value.split('_')
        try {
            val place = args[0].toInt() - 1
            val potionEffect = player.activePotionEffects.toList().getOrNull(place) ?: return null

            return if (args.size == 1) {
                potionEffect.toPotionEffectData().toString()
            } else {
                when (args[1].lowercase()) {
                    "type" -> potionEffect.type.name
                    "amplifier" -> potionEffect.amplifier.toString()
                    "duration" -> potionEffect.duration.toString()
                    "isinfinite" -> potionEffect.isInfinite.toString()
                    else -> null
                }
            }
        } catch (e: NumberFormatException) {
            return null
        }
    }

    private fun handleLocation(player: Player?, value: String): String? {
        if (player == null) return null

        return when (value.lowercase()) {
            "world" -> player.location.world.name
            "x" -> player.location.x.toString()
            "y" -> player.location.y.toString()
            "z" -> player.location.z.toString()
            "yaw" -> player.location.yaw.toString()
            "pitch" -> player.location.pitch.toString()
            "blockx" -> player.location.blockX.toString()
            "blocky" -> player.location.blockY.toString()
            "blockz" -> player.location.blockZ.toString()
            else -> player.location.toString()
        }
    }

    private fun handleExperience(player: Player?, value: String): String? {
        if (player == null) return null

        val experience = expTracker.getExperience(player)

        return when (value.lowercase()) {
            "level" -> experience.first.toString()
            "totalexp" -> experience.second.toString()
            "currentexp" -> experience.third.toString()
            else -> null
        }
    }

    private fun handleBlockInteractions(player: Player?, value: String): String? {
        if (player == null) return null

        val blockInteractions = blockTracker.getBlockInteractions(player)

        val args = value.split('_')
        try {
            val place = args[0].toInt() - 1
            val blockInteraction = blockInteractions.getOrNull(place) ?: return null

            return if (args.size == 1) {
                blockInteraction.toString()
            } else {
                when (args[1].lowercase()) {
                    "type" -> blockInteraction.type
                    "blockbefore" -> blockInteraction.blockBefore
                    "blockafter" -> blockInteraction.blockAfter
                    "location" -> blockInteraction.location.toString()
                    else -> null
                }
            }
        } catch (e: NumberFormatException) {
            return null
        }
    }

    private fun handleMovements(player: Player?, value: String): String? {
        if (player == null) return null

        val movements = movementTracker.getMovements(player)

        val args = value.split('_')
        try {
            val world = Bukkit.getWorld(args[0]) ?: return null
            val place = args[1].toInt()
            val movement = movements[world.name]?.getOrNull(place) ?: return null

            return when (args[2].lowercase()) {
                "x" -> movement.x.toString()
                "y" -> movement.y.toString()
                "z" -> movement.z.toString()
                "timestamp" -> movement.timestamp.toString()
                else -> movement.toString()
            }
        } catch (e: IndexOutOfBoundsException) {
            return null
        } catch (e: NumberFormatException) {
            return null
        }
    }

    private fun handleStatisticTop(value: String): String? {
        val args = value.splitFromEnd('_', limit = 2)
        if (args.size < 2) return null

        val values = args[0].split(':')
        val statistic = if (values.size >= 2) {
            Statistic.entries.firstOrNull { it.name.equals(values[0], true) }
        } else {
            Statistic.entries.firstOrNull { it.name.equals(args[0], true) }
        } ?: return null

        return try {
            val place = args[1].toInt()
            val undefined = configManager.config.getString("placeholder.undefined", "Undefined")

            when (val type = values.getOrNull(1)?.getType()) {
                is EntityType, is Material -> {
                    statistic.getTop(type).getOrNull(place - 1)?.name ?: undefined
                }
                else -> {
                    statistic.getTop().getOrNull(place - 1)?.name ?: undefined
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun extractValue(template: String, value: String): String? {
        val regex = Regex("%([^%]+)%")
        if (!template.contains('%')) {
            return if (template == value) "" else null
        }

        val matchResult = regex.find(template) ?: return null
        val placeholder = matchResult.groupValues[1]

        return if (template == value) "" else {
            val modifiedTemplate = template.replace("%$placeholder%", "([^/]+)")
            Regex(modifiedTemplate).matchEntire(value)?.groupValues?.get(1)
        }
    }

    private fun String.getType(): Any? {
        return EntityType.entries.firstOrNull { it.name.equals(this, true) }
            ?: Material.entries.firstOrNull { it.name.equals(this, true) }
    }

    private fun Statistic.getTop(type: Any? = null): List<OfflinePlayer> {
        val statisticGetter: (OfflinePlayer) -> Int = when (type) {
            is EntityType -> { player -> player.getStatistic(this, type) }
            is Material -> { player -> player.getStatistic(this, type) }
            else -> { player -> player.getStatistic(this) }
        }

        return Bukkit.getOfflinePlayers()
            .filter { statisticGetter(it) > 0 }
            .sortedByDescending { statisticGetter(it) }
    }
}