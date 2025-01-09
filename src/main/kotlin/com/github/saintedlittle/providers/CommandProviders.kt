package com.github.saintedlittle.providers

import com.github.saintedlittle.application.JsonManager
import com.github.saintedlittle.commands.SynchronizeCommand
import com.github.saintedlittle.messaging.KafkaProducerService
import com.google.inject.Inject
import com.google.inject.Provider
import kotlinx.coroutines.CoroutineScope
import org.slf4j.Logger

class SynchronizeCommandProvider @Inject constructor(
    private val kafkaProducerService: KafkaProducerService,
    private val jsonManager: JsonManager,
    private val scope: CoroutineScope,
    private val logger: Logger
) : Provider<SynchronizeCommand> {
    override fun get(): SynchronizeCommand {
        return SynchronizeCommand(kafkaProducerService, jsonManager, scope, logger)
    }
}