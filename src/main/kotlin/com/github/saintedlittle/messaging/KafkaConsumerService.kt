package com.github.saintedlittle.messaging

import com.github.saintedlittle.annotations.KafkaEvent
import com.github.saintedlittle.application.ConfigManager
import com.google.inject.Inject
import kotlinx.coroutines.*
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.Logger
import java.time.Duration

class KafkaConsumerService @Inject constructor(
    configManager: ConfigManager,
    scope: CoroutineScope,
    private val logger: Logger
) {
    private val listeners = mutableMapOf<KafkaTopic, MutableList<(KafkaEventData) -> Unit>>()
    private val consumer: KafkaConsumer<String, String>

    init {
        val props = configManager.kafkaConsumerConfig.apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "${this["ip"]}:${this["port"]}")
        }
        consumer = KafkaConsumer(props)

        consumer.subscribe(KafkaTopic.entries.map { it.topicName })

        /*
        Здесь возникает проблема, что плагин выключается
        А оно продолжает работу (в том плане, что оно успело
        Войти в цикл до срабатывания метода cancel и в итоге вылезла ошибка
        java.lang.IllegalStateException: zip file closed)

        Что делать?
         */
        scope.launch {
            while (isActive) {
                val records = consumer.poll(Duration.ofMillis(1000))
                for (record in records) {
                    logger.debug("Received message: topic=${record.topic()}, key=${record.key()}, value=${record.value()}")
                    triggerEvent(getTopic(record.topic()), record.key(), record.value())
                }
                delay(100)
            }
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
        consumer.close()
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