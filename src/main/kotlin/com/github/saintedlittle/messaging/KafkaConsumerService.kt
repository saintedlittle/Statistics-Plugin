package com.github.saintedlittle.messaging

import com.github.saintedlittle.annotations.KafkaEvent
import com.github.saintedlittle.application.ConfigManager
import com.google.inject.Inject
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.Logger
import java.time.Duration

class KafkaConsumerService @Inject constructor(
    configManager: ConfigManager,
    private val logger: Logger
) {
    private val listeners = mutableMapOf<KafkaTopic, MutableList<(KafkaEventData) -> Unit>>()
    private var consumer: KafkaConsumer<String, String>?
    private val thread: Thread?

    private val isEnabled = configManager.config.getBoolean("kafka.enabled", true)

    init {
        if (isEnabled) {
            val props = configManager.kafkaConsumerConfig.apply {
                put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "${this["ip"]}:${this["port"]}")
            }

            val classLoader = Thread.currentThread().contextClassLoader
            try {
                Thread.currentThread().contextClassLoader = null
                consumer = KafkaConsumer(props)
            } finally {
                Thread.currentThread().contextClassLoader = classLoader
            }

            consumer?.subscribe(KafkaTopic.entries.map { it.topicName })

            thread = Thread {
                try {
                    while (!Thread.currentThread().isInterrupted) {
                        val records = consumer?.poll(Duration.ofMillis(1000)) ?: ConsumerRecords.empty()
                        for (record in records) {
                            logger.debug("Received message: topic=${record.topic()}, key=${record.key()}, value=${record.value()}")
                            triggerEvent(getTopic(record.topic()), record.key(), record.value())
                        }
                        Thread.sleep(100)
                    }
                } catch (e: InterruptedException) {
                    logger.debug("Kafka polling loop interrupted")
                    Thread.currentThread().interrupt()
                } catch (e: Exception) {
                    logger.error("Error in Kafka polling loop", e)
                }
            }

            thread.start()
        } else {
            consumer = null
            thread = null
            logger.warn("Kafka is disabled!")
        }
    }

    fun registerListener(listener: KafkaEventListener) {
        listener::class.java.methods.forEach { method ->
            val annotation = method.getAnnotation(KafkaEvent::class.java)
            if (annotation != null) {
                val topic = annotation.topic
                method.isAccessible = true
                listeners.computeIfAbsent(topic) { mutableListOf() }
                    .add { kafkaEventData ->
                        method.invoke(listener, kafkaEventData)
                    }
            }
        }
    }

    fun close() {
        thread?.interrupt()
        consumer?.close()
    }

    private fun triggerEvent(topic: KafkaTopic?, key: String, value: String) {
        if (topic == null) return
        listeners[topic]?.forEach { handler -> handler(KafkaEventData(topic, key, value)) }
    }

    private fun getTopic(name: String): KafkaTopic? = KafkaTopic.entries.firstOrNull { it.topicName.equals(name, true) }
}

data class KafkaEventData(
    val topic: KafkaTopic,
    val key: String,
    val value: String
)

enum class KafkaTopic(val topicName: String) {
    PLAYER_PAYLOAD("player-payload")
}

interface KafkaEventListener