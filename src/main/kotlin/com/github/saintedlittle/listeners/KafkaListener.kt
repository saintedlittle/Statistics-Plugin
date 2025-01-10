package com.github.saintedlittle.listeners

import com.github.saintedlittle.annotations.AutoRegister
import com.github.saintedlittle.annotations.KafkaEvent
import com.github.saintedlittle.messaging.KafkaEventData
import com.github.saintedlittle.messaging.KafkaEventListener
import com.github.saintedlittle.messaging.KafkaTopic
import com.google.inject.Inject
import org.slf4j.Logger

@AutoRegister
class KafkaListener @Inject constructor(
    private val logger: Logger
) : KafkaEventListener {
    @KafkaEvent(topic = KafkaTopic.PLAYER_PAYLOAD)
    fun onPlayerPayload(event: KafkaEventData) {
        logger.info("New payload: topic=${event.topic} key=${event.key} value=${event.value}")
    }
}