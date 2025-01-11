package com.github.saintedlittle.listeners

import com.github.saintedlittle.annotations.AutoRegister
import com.github.saintedlittle.annotations.KafkaEvent
import com.github.saintedlittle.application.JsonManager
import com.github.saintedlittle.application.JsonUtil
import com.github.saintedlittle.messaging.*
import com.github.saintedlittle.messaging.data.Payload
import com.github.saintedlittle.messaging.data.PayloadRequest
import com.github.saintedlittle.messaging.data.PayloadResponse
import com.github.saintedlittle.messaging.data.ResponseStatus
import com.google.inject.Inject
import org.bukkit.Bukkit
import org.slf4j.Logger
import java.util.UUID

@AutoRegister
class KafkaListener @Inject constructor(
    private val kafkaProducerService: KafkaProducerService,
    private val jsonManager: JsonManager,
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
            Payload.GET_FULL_DATA -> handleGetFullData(request.playerId)
            else -> ResponseStatus.NOT_FOUND to emptyMap()
        }

        return PayloadResponse(
            payload = request.payload.id,
            playerId = request.playerId.toString(),
            key = request.key,
            data = data,
            status = status.id
        )
    }

    private fun handleGetUsername(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        val player = Bukkit.getOfflinePlayer(playerId)
        return if (player.name != null) {
            ResponseStatus.SUCCESS to mapOf("username" to player.name!!)
        } else {
            ResponseStatus.NOT_FOUND to emptyMap()
        }
    }

    private fun handleGetFullData(playerId: UUID): Pair<ResponseStatus, Map<String, String>> {
        val player = Bukkit.getPlayer(playerId)
        return if (player != null) {
            ResponseStatus.SUCCESS to mapOf("data" to jsonManager.createPlayerJson(player, readable = false))
        } else {
            ResponseStatus.NOT_FOUND to emptyMap()
        }
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
}
