package com.github.saintedlittle

import com.github.saintedlittle.application.JsonManager
import com.github.saintedlittle.domain.*
import com.github.saintedlittle.listeners.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class Main : JavaPlugin() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var movementTracker: MovementTracker
    private lateinit var timeTracker: PlayerTimeTracker
    private lateinit var bedTracker: BedTracker

    override fun onEnable() {
        if (!dataFolder.exists()) dataFolder.mkdir()
        val configFile = File(dataFolder, "config.json")
        if (!configFile.exists()) ConfigManager.saveDefaultConfig(configFile)
        ConfigManager.init(configFile)

        timeTracker = PlayerTimeTracker(scope, dataFolder.path)
        movementTracker = MovementTracker(scope, dataFolder.path)
        bedTracker = BedTracker(dataFolder.path)

        val jsonManager = JsonManager(timeTracker, bedTracker, movementTracker)

        server.pluginManager.registerEvents(PlayerListener(timeTracker, jsonManager, scope), this)
        server.pluginManager.registerEvents(BlockListener(bedTracker), this)
        server.pluginManager.registerEvents(MovementListener(movementTracker), this)

        logger.info("Plugin enabled.")
    }

    override fun onDisable() {
        movementTracker.close()
        timeTracker.close()
        bedTracker.close()
        scope.cancel()
        logger.info("Plugin disabled.")
    }

}
