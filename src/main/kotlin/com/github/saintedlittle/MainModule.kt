package com.github.saintedlittle

import com.github.saintedlittle.application.ConfigManager
import com.github.saintedlittle.application.JsonManager
import com.github.saintedlittle.domain.BedTracker
import com.github.saintedlittle.domain.MovementTracker
import com.github.saintedlittle.domain.PlayerTimeTracker
import com.google.inject.AbstractModule
import kotlinx.coroutines.CoroutineScope
import org.bukkit.plugin.Plugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MainModule(
    private val plugin: Plugin,
    private val scope: CoroutineScope,
    private val configManager: ConfigManager
) : AbstractModule() {

    override fun configure() {
        bind(Plugin::class.java).toInstance(plugin)
        bind(CoroutineScope::class.java).toInstance(scope)
        bind(ConfigManager::class.java).toInstance(configManager)
        bind(Logger::class.java).toInstance(LoggerFactory.getLogger(plugin::class.java))

        bind(PlayerTimeTracker::class.java).toInstance(PlayerTimeTracker(scope, plugin.dataFolder.path))
        bind(MovementTracker::class.java).toInstance(MovementTracker(scope, plugin.dataFolder.path, configManager))
        bind(BedTracker::class.java).toInstance(BedTracker(plugin.dataFolder.path))

        bind(JsonManager::class.java).toInstance(
            JsonManager(
                PlayerTimeTracker(scope, plugin.dataFolder.path),
                BedTracker(plugin.dataFolder.path),
                MovementTracker(scope, plugin.dataFolder.path, configManager)
            )
        )
    }
}
