package com.github.saintedlittle.utils

import com.github.saintedlittle.annotations.AutoRegister
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.reflections.Reflections
import com.google.inject.Injector

object ListenerRegistrar {
    fun registerAll(plugin: Plugin, injector: Injector) {
        val reflections = Reflections("com.github.saintedlittle.listeners")
        val listeners = reflections.getTypesAnnotatedWith(AutoRegister::class.java)
        listeners.forEach { listenerClass ->
            val listener = injector.getInstance(listenerClass) as Listener
            plugin.server.pluginManager.registerEvents(listener, plugin)
            plugin.logger.info("Registered listener: ${listenerClass.simpleName}")
        }
    }
}
