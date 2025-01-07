package com.github.saintedlittle

import com.github.saintedlittle.application.ConfigManager
import com.github.saintedlittle.application.JsonManager
import com.github.saintedlittle.domain.BedTracker
import com.github.saintedlittle.domain.MovementTracker
import com.github.saintedlittle.domain.PlayerTimeTracker
import com.github.saintedlittle.listeners.BlockListener
import com.github.saintedlittle.listeners.MovementListener
import com.github.saintedlittle.listeners.PlayerListener
import com.google.inject.AbstractModule
import kotlinx.coroutines.CoroutineScope
import org.bukkit.plugin.Plugin
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.config.units.MemoryUnit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import kotlin.jvm.java
import com.google.inject.Inject
import com.google.inject.Provider

import javax.inject.Singleton

class MainModule(
    private val plugin: Plugin,
    private val scope: CoroutineScope,
    private val configManager: ConfigManager
) : AbstractModule() {

    override fun configure() {
        // Создание CacheManager
        val cacheManager: CacheManager = CacheManagerBuilder.newCacheManagerBuilder()
            .with(CacheManagerBuilder.persistence(File(plugin.dataFolder, "ehcache")))
            .build(true)

        // Конфигурации кэшей
        val playerTimesCache = createCache<UUID, java.lang.Long>(cacheManager, "playerTimes", 1000)
        val playerSessionStartCache = createCache<UUID, java.lang.Long>(cacheManager, "playerSessionStart", 1000)
        val playerMovementsCache = createCache<UUID, String>(cacheManager, "playerMovements", 70000)
        val playerBedsCache = createCache<UUID, String>(cacheManager, "playerBeds", 7000)

        // Привязки базовых зависимостей
        bind(Plugin::class.java).toInstance(plugin)
        bind(CoroutineScope::class.java).toInstance(scope)
        bind(ConfigManager::class.java).toInstance(configManager)
        bind(Logger::class.java).toInstance(LoggerFactory.getLogger(plugin::class.java))
        bind(CacheManager::class.java).toInstance(cacheManager)

        // Привязки для трекеров
        bind(PlayerTimeTracker::class.java).toInstance(
            PlayerTimeTracker(scope, playerTimesCache, playerSessionStartCache)
        )
        bind(MovementTracker::class.java).toInstance(
            MovementTracker(scope, playerMovementsCache, configManager)
        )
        bind(BedTracker::class.java).toInstance(
            BedTracker(playerBedsCache)
        )

        // JsonManager
        bind(JsonManager::class.java).toInstance(
            JsonManager(
                PlayerTimeTracker(scope, playerTimesCache, playerSessionStartCache),
                BedTracker(playerBedsCache),
                MovementTracker(scope, playerMovementsCache, configManager)
            )
        )

        // Привязки для слушателей (автоматическая инъекция)
        bind(MovementListener::class.java).toProvider(MovementListenerProvider::class.java)
        bind(PlayerListener::class.java).toProvider(PlayerListenerProvider::class.java)
        bind(BlockListener::class.java).toProvider(BlockListenerProvider::class.java)

    }

    // Вспомогательный метод для создания кэша
    private inline fun <reified K, reified V> createCache(
        cacheManager: CacheManager,
        cacheName: String,
        heapSize: Long
    ): Cache<K, V> {
        return cacheManager.createCache(
            cacheName,
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                K::class.java,
                V::class.java,
                ResourcePoolsBuilder.heap(heapSize).disk(5, MemoryUnit.GB, true)
            )
        )
    }
}


class MovementListenerProvider @Inject constructor(
    private val movementTracker: MovementTracker,
    private val logger: Logger
) : Provider<MovementListener> {
    override fun get(): MovementListener {
        return MovementListener(movementTracker, logger)
    }
}

class PlayerListenerProvider @Inject constructor(
    private val tracker: PlayerTimeTracker,
    private val jsonManager: JsonManager,
    private val scope: CoroutineScope,
    private val logger: Logger
) : Provider<PlayerListener> {
    override fun get(): PlayerListener {
        return PlayerListener(tracker, jsonManager, scope, logger)
    }
}

class BlockListenerProvider @Inject constructor(
    private val bedTracker: BedTracker,
    private val logger: Logger
) : Provider<BlockListener> {
    override fun get(): BlockListener {
        return BlockListener(bedTracker, logger)
    }
}