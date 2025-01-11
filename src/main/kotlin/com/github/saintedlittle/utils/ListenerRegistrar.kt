package com.github.saintedlittle.utils

import com.github.saintedlittle.annotations.AutoRegister
import com.github.saintedlittle.messaging.KafkaConsumerService
import com.github.saintedlittle.messaging.KafkaEventListener
import com.google.inject.Injector
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.reflections.Reflections

object ListenerRegistrar {
    fun registerAll(plugin: Plugin, injector: Injector) {
        val reflections = Reflections("com.github.saintedlittle.listeners")
        val listenerClasses = reflections.getTypesAnnotatedWith(AutoRegister::class.java)

        listenerClasses.forEach { listenerClass ->
            try {
                when (val instance = injector.getInstance(listenerClass)) {
                    is Listener -> {
                        plugin.server.pluginManager.registerEvents(instance, plugin)
                        plugin.logger.info("Registered bukkit listener: ${listenerClass.simpleName}")
                    }

                    is KafkaEventListener -> {
                        injector.getInstance(KafkaConsumerService::class.java).registerListener(instance)
                        plugin.logger.info("Registered kafka listener: ${listenerClass.simpleName}")
                    }

                    else ->
                        plugin.logger.warning(
                            "Class ${listenerClass.simpleName} is annotated with @AutoRegister but does not implement Listener or KafkaEventListener."
                        )
                }
            } catch (e: Exception) {
                plugin.logger.severe("Error registering listener ${listenerClass.simpleName}: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
