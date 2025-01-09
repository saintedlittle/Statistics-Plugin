package com.github.saintedlittle

import com.github.saintedlittle.application.ConfigManager
import com.github.saintedlittle.utils.CommandRegistrar
import com.github.saintedlittle.utils.ListenerRegistrar
import com.google.inject.Guice
import com.google.inject.Injector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.bukkit.plugin.java.JavaPlugin
import org.ehcache.CacheManager

class Main : JavaPlugin() {

    private lateinit var injector: Injector
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onEnable() {
        if (!dataFolder.exists()) dataFolder.mkdir()
        val configManager = ConfigManager(dataFolder)

        injector = Guice.createInjector(MainModule(this, scope, configManager))

        ListenerRegistrar.registerAll(this, injector)
        CommandRegistrar.registerAll(this, injector)

        logger.info("Plugin successfully enabled.")
    }

    override fun onDisable() {
        injector.getInstance(CacheManager::class.java).close()
        scope.cancel()

        logger.info("Plugin successfully disabled.")
    }
}
