package com.github.saintedlittle.providers

import com.github.saintedlittle.application.ConfigManager
import com.github.saintedlittle.messaging.KafkaProducerService
import com.google.inject.Inject
import com.google.inject.Provider
import org.slf4j.Logger

class KafkaProducerServiceProvider @Inject constructor(
    private val configManager: ConfigManager,
    private val logger: Logger
) : Provider<KafkaProducerService> {
    override fun get(): KafkaProducerService {
        return KafkaProducerService(configManager, logger)
    }
}