package com.github.saintedlittle.messaging

import com.github.saintedlittle.application.ConfigManager
import com.google.inject.Inject
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger

class KafkaProducerService @Inject constructor(
    configManager: ConfigManager,
    private val logger: Logger
) {
    private var producer: KafkaProducer<String, String>?

    val isEnabled = configManager.config.getBoolean("kafka.enabled", true)

    init {
        if (isEnabled) {
            val props = configManager.kafkaProducerConfig.apply {
                put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "${this["ip"]}:${this["port"]}")
            }
            // ConfigException: Invalid value StringSerializer for configuration key.serializer: Class StringSerializer could not be found.
            val classLoader = Thread.currentThread().contextClassLoader
            try {
                Thread.currentThread().contextClassLoader = null
                producer = KafkaProducer(props)
            } finally {
                Thread.currentThread().contextClassLoader = classLoader
            }
        } else producer = null
    }

    private enum class KafkaTopic(val topicName: String) {
        PLAYER_SYNC("player-data-sync"),
        PLAYER_LOGIN("player-login-events"),
        PLAYER_LOGOUT("player-logout-events"),
        PAYLOAD_RESPONSE("payload-response")
    }

    fun sendPlayerSync(key: String, message: String) {
        sendMessage(KafkaTopic.PLAYER_SYNC, key, message)
    }

    fun sendPlayerLogin(key: String, message: String) {
        sendMessage(KafkaTopic.PLAYER_LOGIN, key, message)
    }

    fun sendPlayerLogout(key: String, message: String) {
        sendMessage(KafkaTopic.PLAYER_LOGOUT, key, message)
    }

    fun sendPayloadResponse(key: String, message: String) {
        sendMessage(KafkaTopic.PAYLOAD_RESPONSE, key, message)
    }

    fun close() {
        producer?.close()
    }

    private fun sendMessage(topic: KafkaTopic, key: String, message: String) {
        if (!isEnabled) return

        try {
            val record = ProducerRecord(topic.topicName, key, message)
            producer?.send(record) { metadata, exception ->
                if (exception != null) {
                    logger.error("Failed to send message to topic ${topic.topicName}", exception)
                } else {
                    logger.debug("Message sent to topic ${topic.topicName}: offset=${metadata.offset()}, partition=${metadata.partition()}")
                }
            }
        } catch (e: Exception) {
            logger.error("Error while sending message to Kafka topic ${topic.topicName}", e)
        }
    }
}