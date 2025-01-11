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
        logger.debug("New payload: topic={} key={} value={}", event.topic, event.key, event.value)

        try {
            val payloadJson: MutableMap<String, String> = JsonUtil.fromJson(event.value)
            val key = event.key
            val payload = Payload.of(payloadJson.remove("payload"))
            val playerId = payloadJson.remove("playerId")

            if (payload == Payload.UNKNOWN || playerId == null) {
                logger.debug("Failed to receive payload")
                return
            }

            val request = PayloadRequest(
                payload = payload,
                playerId = UUID.fromString(playerId),
                key = key,
                data = payloadJson
            )
            val response = handleRequest(request)

            sendResponse(response)
        } catch (e: Exception) {
            logger.error("Error processing payload: topic=${event.topic} key=${event.key} value=${event.value}", e)
        }
    }

    private fun handleRequest(request: PayloadRequest): PayloadResponse {
        val data = mutableMapOf<String, String>()
        val status: ResponseStatus

        when (request.payload) {
            Payload.GET_USERNAME -> {
                val player = Bukkit.getOfflinePlayer(request.playerId)
                if (player.name != null) {
                    data["username"] = player.name!!
                    status = ResponseStatus.SUCCESS
                } else status = ResponseStatus.NOT_FOUND
            }

            Payload.GET_FULL_DATA -> {
                val player = Bukkit.getPlayer(request.playerId)
                if (player != null) {
                    data["data"] = jsonManager.createPlayerJson(player, readable = false)
                    status = ResponseStatus.SUCCESS
                } else status = ResponseStatus.NOT_FOUND
            }

            else -> {
                status = ResponseStatus.NOT_FOUND
            }
        }

        return PayloadResponse(
            payload = request.payload.id,
            playerId = request.playerId.toString(),
            key = request.key,
            data = data,
            status = status.id
        )
    }

    private fun sendResponse(response: PayloadResponse) {
        val data = JsonUtil.toJson(response)
        kafkaProducerService.sendPayloadResponse(response.key, data)
    }
}

