package com.github.saintedlittle.providers

import com.github.saintedlittle.application.JsonManager
import com.github.saintedlittle.domain.*
import com.github.saintedlittle.listeners.BlockListener
import com.github.saintedlittle.listeners.KafkaListener
import com.github.saintedlittle.listeners.MovementListener
import com.github.saintedlittle.listeners.PlayerEventListener
import com.github.saintedlittle.messaging.KafkaProducerService
import com.google.inject.Inject
import com.google.inject.Provider
import kotlinx.coroutines.CoroutineScope
import org.slf4j.Logger

class MovementListenerProvider @Inject constructor(
    private val movementTracker: MovementTracker,
    private val logger: Logger
) : Provider<MovementListener> {
    override fun get(): MovementListener {
        return MovementListener(movementTracker, logger)
    }
}

class BlockListenerProvider @Inject constructor(
    private val blockTracker: BlockTracker,
    private val bedTracker: BedTracker,
    private val logger: Logger
) : Provider<BlockListener> {
    override fun get(): BlockListener {
        return BlockListener(blockTracker, bedTracker, logger)
    }
}

class PlayerEventListenerProvider @Inject constructor(
    private val tracker: PlayerTimeTracker,
    private val kafkaProducerService: KafkaProducerService,
    private val jsonManager: JsonManager,
    private val expTracker: ExpTracker,
    private val scope: CoroutineScope,
    private val logger: Logger
) : Provider<PlayerEventListener> {
    override fun get(): PlayerEventListener {
        return PlayerEventListener(tracker, kafkaProducerService, jsonManager, expTracker, scope, logger)
    }
}

class KafkaListenerProvider @Inject constructor(
    private val kafkaProducerService: KafkaProducerService,
    private val jsonManager: JsonManager,
    private val timeTracker: PlayerTimeTracker,
    private val expTracker: ExpTracker,
    private val blockTracker: BlockTracker,
    private val bedTracker: BedTracker,
    private val movementTracker: MovementTracker,
    private val logger: Logger
) : Provider<KafkaListener> {
    override fun get(): KafkaListener {
        return KafkaListener(kafkaProducerService, jsonManager, timeTracker, expTracker, blockTracker, bedTracker, movementTracker, logger)
    }
}