package com.github.saintedlittle

import com.github.saintedlittle.application.ConfigManager
import com.github.saintedlittle.domain.BlockTracker
import com.github.saintedlittle.domain.ExpTracker
import com.github.saintedlittle.domain.MovementTracker
import com.github.saintedlittle.messaging.KafkaConsumerService
import com.github.saintedlittle.messaging.KafkaProducerService
import com.github.saintedlittle.placeholderapi.Placeholder
import com.github.saintedlittle.utils.CommandRegistrar
import com.github.saintedlittle.utils.ListenerRegistrar
import com.google.inject.Guice
import com.google.inject.Injector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.ehcache.CacheManager
import org.slf4j.Logger

class Main : JavaPlugin() {

    private lateinit var injector: Injector
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onEnable() {
        if (!dataFolder.exists()) dataFolder.mkdir()
        val configManager = ConfigManager(dataFolder)

        injector = Guice.createInjector(MainModule(this, scope, configManager))

        ListenerRegistrar.registerAll(this, injector)
        CommandRegistrar.registerAll(this, injector)

        val logger = injector.getInstance(Logger::class.java)

        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") ||
            configManager.placeholderConfig["enabled"] != "true")
            logger.warn("Failed to register placeholder: plugin PlaceholderAPI is disabled!")
        else Placeholder(
            configManager,
            injector.getInstance(ExpTracker::class.java),
            injector.getInstance(BlockTracker::class.java),
            injector.getInstance(MovementTracker::class.java),
            logger
        ).register()

        this.logger.info("Plugin successfully enabled.")
    }

    override fun onDisable() {
        injector.getInstance(CacheManager::class.java).close()
        injector.getInstance(KafkaProducerService::class.java).close()
        injector.getInstance(KafkaConsumerService::class.java).close()
        scope.cancel()

        logger.info("Plugin successfully disabled.")
    }
}
