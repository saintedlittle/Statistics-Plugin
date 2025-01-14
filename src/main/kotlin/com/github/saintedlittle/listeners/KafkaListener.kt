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
import com.github.saintedlittle.models.PlayerWrapper
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

    private fun <T> withPlayer(playerId: UUID, block: (PlayerWrapper) -> T): T {
        val player = Bukkit.getPlayer(playerId) ?: Bukkit.getOfflinePlayer(playerId)
        return block(PlayerWrapper(player))
    }

    private fun handleGetUsername(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            player.name?.let { username ->
                ResponseStatus.SUCCESS to mapOf("username" to username)
            } ?: NOT_FOUND_RESPONSE
        }
    }

    private fun handleMetaData(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            val metaData = player.metadata ?: return@withPlayer null
            ResponseStatus.SUCCESS to mapOf("metadata" to JsonUtil.toJson(metaData, readable = false))
        } ?: NOT_FOUND_RESPONSE
    }

    private fun handleGetInventory(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            val inventory = player.inventory ?: return@withPlayer null
            ResponseStatus.SUCCESS to mapOf("inventory" to JsonUtil.toJson(inventory, readable = false))
        } ?: NOT_FOUND_RESPONSE
    }

    private fun handleGetArmor(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            val armor = player.armor ?: return@withPlayer null
            ResponseStatus.SUCCESS to mapOf("armor" to JsonUtil.toJson(armor, readable = false))
        } ?: NOT_FOUND_RESPONSE
    }

    private fun handleGetStatistics(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            ResponseStatus.SUCCESS to mapOf("statistics" to JsonUtil.toJson(player.statistics, readable = false))
        }
    }

    private fun handleGetAttributes(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            val attributes = player.attributes ?: return@withPlayer null
            ResponseStatus.SUCCESS to mapOf("attributes" to JsonUtil.toJson(attributes, readable = false))
        } ?: NOT_FOUND_RESPONSE
    }

    private fun handlePotionEffects(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            val potionEffects = player.potionEffects ?: return@withPlayer null
            ResponseStatus.SUCCESS to mapOf("potionEffects" to JsonUtil.toJson(potionEffects, readable = false))
        } ?: NOT_FOUND_RESPONSE
    }

    private fun handleLocation(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            val location = player.location ?: return@withPlayer null
            ResponseStatus.SUCCESS to mapOf("location" to JsonUtil.toJson(location, readable = false))
        } ?: NOT_FOUND_RESPONSE
    }

    private fun handleTotalTime(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            val totalTime = player.totalTime(timeTracker) ?: return@withPlayer null
            ResponseStatus.SUCCESS to mapOf("totalTime" to totalTime.toString())
        } ?: NOT_FOUND_RESPONSE
    }

    private fun handleLevel(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            val level = player.experience(expTracker)?.first ?: return@withPlayer null
            ResponseStatus.SUCCESS to mapOf("level" to level.toString())
        } ?: NOT_FOUND_RESPONSE
    }

    private fun handleTotalExp(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            val totalExp = player.experience(expTracker)?.second ?: return@withPlayer null
            ResponseStatus.SUCCESS to mapOf("totalExp" to totalExp.toString())
        } ?: NOT_FOUND_RESPONSE
    }

    private fun handleCurrentExp(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            val currentExp = player.experience(expTracker)?.third ?: return@withPlayer null
            ResponseStatus.SUCCESS to mapOf("currentExp" to currentExp.toString())
        } ?: NOT_FOUND_RESPONSE
    }

    private fun handleBlockInteractions(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            val blockInteractions = player.blockInteractions(blockTracker) ?: return@withPlayer null
            ResponseStatus.SUCCESS to mapOf("blockInteractions" to JsonUtil.toJson(blockInteractions, readable = false))
        } ?: NOT_FOUND_RESPONSE
    }

    private fun handleBeds(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            val beds = player.beds(bedTracker) ?: return@withPlayer null
            ResponseStatus.SUCCESS to mapOf("beds" to JsonUtil.toJson(beds, readable = false))
        } ?: NOT_FOUND_RESPONSE
    }

    private fun handleMovements(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            val movements = player.movements(movementTracker) ?: return@withPlayer null
            ResponseStatus.SUCCESS to mapOf("movements" to JsonUtil.toJson(movements, readable = false))
        } ?: NOT_FOUND_RESPONSE
    }

    private fun handleGetFullData(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        return withPlayer(playerId) { player ->
            val playerData = player.createPlayerJson(jsonManager) ?: return@withPlayer null
            ResponseStatus.SUCCESS to mapOf("data" to playerData)
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
