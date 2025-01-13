package com.github.saintedlittle.listeners

import com.github.saintedlittle.annotations.AutoRegister
import com.github.saintedlittle.annotations.KafkaEvent
import com.github.saintedlittle.application.JsonManager
import com.github.saintedlittle.application.JsonUtil
import com.github.saintedlittle.application.toItemData
import com.github.saintedlittle.data.ArmorSlot
import com.github.saintedlittle.data.ItemData
import com.github.saintedlittle.data.LocationData
import com.github.saintedlittle.data.MetaData
import com.github.saintedlittle.domain.*
import com.github.saintedlittle.extensions.collectAttributes
import com.github.saintedlittle.extensions.collectPotionEffects
import com.github.saintedlittle.extensions.collectStatistics
import com.github.saintedlittle.messaging.*
import com.github.saintedlittle.messaging.data.Payload
import com.github.saintedlittle.messaging.data.PayloadRequest
import com.github.saintedlittle.messaging.data.PayloadResponse
import com.github.saintedlittle.messaging.data.ResponseStatus
import com.google.inject.Inject
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.slf4j.Logger
import java.util.UUID

@AutoRegister
class KafkaListener @Inject constructor(
    private val kafkaProducerService: KafkaProducerService,
    private val jsonManager: JsonManager,
    private val timeTracker: PlayerTimeTracker,
    private val expTracker: ExpTracker,
    private val blockTracker: BlockTracker,
    private val bedTracker: BedTracker,
    private val movementTracker: MovementTracker,
    private val logger: Logger
) : KafkaEventListener {

    @KafkaEvent(topic = KafkaTopic.PLAYER_PAYLOAD)
    fun onPlayerPayload(event: KafkaEventData) {
        logger.debug("Received payload: topic={}, key={}, value={}", event.topic, event.key, event.value)

        val payloadRequest = runCatching { parsePayload(event) }
            .onFailure { logger.error("Error parsing payload: topic={}, key={}", event.topic, event.key, it) }
            .getOrNull() ?: return

        val response = handleRequest(payloadRequest)
        sendResponse(response)
    }

    private fun parsePayload(event: KafkaEventData): PayloadRequest {
        val payloadJson: MutableMap<String, String> = JsonUtil.fromJson(event.value)
        val key = event.key
        val payload = Payload.of(payloadJson.remove("payload"))
            ?: throw IllegalArgumentException("Unknown payload type")
        val playerId = payloadJson.remove("playerId")?.let { UUID.fromString(it) }
            ?: throw IllegalArgumentException("Invalid or missing playerId")

        return PayloadRequest(
            payload = payload,
            playerId = playerId,
            key = key,
            data = payloadJson
        )
    }

    private fun handleRequest(request: PayloadRequest): PayloadResponse {
        val (status, data) = when (request.payload) {
            Payload.GET_USERNAME -> handleGetUsername(request.playerId)
            Payload.GET_METADATA -> handleMetaData(request.playerId)
            Payload.GET_INVENTORY -> handleGetInventory(request.playerId)
            Payload.GET_ARMOR -> handleGetArmor(request.playerId)
            Payload.GET_STATISTICS -> handleGetStatistics(request.playerId)
            Payload.GET_ATTRIBUTES -> handleGetAttributes(request.playerId)
            Payload.GET_POTION_EFFECTS -> handlePotionEffects(request.playerId)
            Payload.GET_LOCATION -> handleLocation(request.playerId)
            Payload.GET_TOTAL_TIME -> handleTotalTime(request.playerId)
            Payload.GET_LEVEL -> handleLevel(request.playerId)
            Payload.GET_TOTAL_EXP -> handleTotalExp(request.playerId)
            Payload.GET_CURRENT_EXP -> handleCurrentExp(request.playerId)
            Payload.GET_BLOCK_INTERACTIONS -> handleBlockInteractions(request.playerId)
            Payload.GET_BEDS -> handleBeds(request.playerId)
            Payload.GET_MOVEMENTS -> handleMovements(request.playerId)
            Payload.GET_FULL_DATA -> handleGetFullData(request.playerId)
            else -> NOT_FOUND_RESPONSE
        }

        return PayloadResponse(
            payload = request.payload.id,
            playerId = request.playerId.toString(),
            key = request.key,
            data = data,
            status = status.id
        )
    }

    private fun <T> withPlayer(playerId: UUID, block: (Player) -> T): T? {
        val player = Bukkit.getPlayer(playerId) ?: return null
        return block(player)
    }

    private fun <T> withOfflinePlayer(playerId: UUID, block: (OfflinePlayer) -> T): T {
        val player = Bukkit.getOfflinePlayer(playerId)
        return block(player)
    }

    private fun handleGetUsername(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withOfflinePlayer(playerId) { player ->
            player.name?.let { username ->
                ResponseStatus.SUCCESS to mapOf("username" to username)
            } ?: NOT_FOUND_RESPONSE
        }
    }

    private fun handleMetaData(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            val metaData = MetaData.from(player)
            ResponseStatus.SUCCESS to mapOf("metadata" to JsonUtil.toJson(metaData, readable = false))
        } ?: NOT_FOUND_RESPONSE
    }

    private fun handleGetInventory(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            val inventory = player.inventory.contents.filterNotNull().map { it.toItemData() }
            ResponseStatus.SUCCESS to mapOf("inventory" to JsonUtil.toJson(inventory, readable = false))
        } ?: NOT_FOUND_RESPONSE
    }

    private fun handleGetArmor(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            val armor = player.inventory.armorContents.mapIndexedNotNull { index, item ->
                ArmorSlot.entries.getOrNull(index)?.name?.let { it to (item?.toItemData() ?: ItemData.empty()) }
            }.toMap()
            ResponseStatus.SUCCESS to mapOf("armor" to JsonUtil.toJson(armor, readable = false))
        } ?: NOT_FOUND_RESPONSE
    }

    private fun handleGetStatistics(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withOfflinePlayer(playerId) { player ->
            ResponseStatus.SUCCESS to mapOf("statistics" to JsonUtil.toJson(player.collectStatistics(), readable = false))
        }
    }

    private fun handleGetAttributes(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            ResponseStatus.SUCCESS to mapOf("attributes" to JsonUtil.toJson(player.collectAttributes(), readable = false))
        } ?: NOT_FOUND_RESPONSE
    }

    private fun handlePotionEffects(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            ResponseStatus.SUCCESS to mapOf("potionEffects" to JsonUtil.toJson(player.collectPotionEffects(), readable = false))
        } ?: NOT_FOUND_RESPONSE
    }

    private fun handleLocation(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            ResponseStatus.SUCCESS to mapOf("location" to JsonUtil.toJson(LocationData.from(player.location), readable = false))
        } ?: NOT_FOUND_RESPONSE
    }

    private fun handleTotalTime(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            ResponseStatus.SUCCESS to mapOf("totalTime" to timeTracker.getTotalPlayTime(player).toString())
        } ?: NOT_FOUND_RESPONSE
    }

    private fun handleLevel(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            val level = expTracker.getExperience(player).first
            ResponseStatus.SUCCESS to mapOf("level" to level.toString())
        } ?: NOT_FOUND_RESPONSE
    }

    private fun handleTotalExp(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            val totalExp = expTracker.getExperience(player).second
            ResponseStatus.SUCCESS to mapOf("totalExp" to totalExp.toString())
        } ?: NOT_FOUND_RESPONSE
    }

    private fun handleCurrentExp(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            val currentExp = expTracker.getExperience(player).third
            ResponseStatus.SUCCESS to mapOf("currentExp" to currentExp.toString())
        } ?: NOT_FOUND_RESPONSE
    }

    private fun handleBlockInteractions(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            val blockInteractions = blockTracker.getBlockInteractions(player)
            ResponseStatus.SUCCESS to mapOf("blockInteractions" to JsonUtil.toJson(blockInteractions, readable = false))
        } ?: NOT_FOUND_RESPONSE
    }

    private fun handleBeds(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            val beds = bedTracker.getBeds(player).map { LocationData.from(it) }
            ResponseStatus.SUCCESS to mapOf("beds" to JsonUtil.toJson(beds, readable = false))
        } ?: NOT_FOUND_RESPONSE
    }

    private fun handleMovements(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            val movements = movementTracker.getMovements(player)
            ResponseStatus.SUCCESS to mapOf("movements" to JsonUtil.toJson(movements, readable = false))
        } ?: NOT_FOUND_RESPONSE
    }

    private fun handleGetFullData(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            ResponseStatus.SUCCESS to mapOf("data" to jsonManager.createPlayerJson(player, readable = false))
        } ?: NOT_FOUND_RESPONSE
    }

    private fun sendResponse(response: PayloadResponse) {
        runCatching {
            val data = JsonUtil.toJson(response)
            kafkaProducerService.sendPayloadResponse(response.key, data)
            logger.debug("Response sent: key={}, status={}", response.key, response.status)
        }.onFailure {
            logger.error("Failed to send response: key=${response.key}", it)
        }
    }

    companion object {
        private val NOT_FOUND_RESPONSE = ResponseStatus.NOT_FOUND to emptyMap<String, String>()
    }
}
