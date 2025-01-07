package com.github.saintedlittle

import com.github.saintedlittle.application.ConfigManager
import com.github.saintedlittle.domain.BedTracker
import com.github.saintedlittle.domain.MovementTracker
import com.github.saintedlittle.domain.PlayerTimeTracker
import com.github.saintedlittle.utils.ListenerRegistrar
import com.google.inject.Guice
import com.google.inject.Injector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class Main : JavaPlugin() {

    private lateinit var injector: Injector
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onEnable() {
        if (!dataFolder.exists()) dataFolder.mkdir()
        val configManager = ConfigManager(dataFolder)

        injector = Guice.createInjector(MainModule(this, scope, configManager))

        ListenerRegistrar.registerAll(this, injector)

        logger.info("Plugin successfully enabled.")
    }

    override fun onDisable() {
        injector.getInstance(MovementTracker::class.java).close()
        injector.getInstance(PlayerTimeTracker::class.java).close()
        injector.getInstance(BedTracker::class.java).close()
        scope.cancel()

        logger.info("Plugin successfully disabled.")
    }
}
