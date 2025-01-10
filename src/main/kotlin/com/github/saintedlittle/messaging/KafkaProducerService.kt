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
    private val producer: KafkaProducer<String, String>

    init {
        val props = configManager.kafkaProducerConfig.apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "${this["ip"]}:${this["port"]}")
        }
        // ConfigException: Invalid value StringSerializer for configuration key.serializer: Class StringSerializer could not be found.
        Thread.currentThread().setContextClassLoader(null)
        producer = KafkaProducer(props)
    }

    enum class KafkaTopic(val topicName: String) {
        PLAYER_SYNC("player-data-sync")
    }

    fun sendPlayerSync(key: String, message: String) {
        sendMessage(KafkaTopic.PLAYER_SYNC, key, message)
    }

    fun close() {
        producer.close()
    }

    private fun sendMessage(topic: KafkaTopic, key: String, message: String) {
        try {
            val record = ProducerRecord(topic.topicName, key, message)
            producer.send(record) { metadata, exception ->
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