package com.github.saintedlittle.providers

import com.github.saintedlittle.application.ConfigManager
import com.github.saintedlittle.messaging.KafkaConsumerService
import com.github.saintedlittle.messaging.KafkaProducerService
import com.google.inject.Inject
import com.google.inject.Provider
import kotlinx.coroutines.CoroutineScope
import org.slf4j.Logger

class KafkaProducerServiceProvider @Inject constructor(
    private val configManager: ConfigManager,
    private val logger: Logger
) : Provider<KafkaProducerService> {
    override fun get(): KafkaProducerService {
        return KafkaProducerService(configManager, logger)
    }
}

class KafkaConsumerServiceProvider @Inject constructor(
    private val configManager: ConfigManager,
    private val scope: CoroutineScope,
    private val logger: Logger
) : Provider<KafkaConsumerService> {
    override fun get(): KafkaConsumerService {
        return KafkaConsumerService(configManager, scope, logger)
    }
}