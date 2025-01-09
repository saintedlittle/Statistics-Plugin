package com.github.saintedlittle.utils

import com.github.saintedlittle.annotations.AutoRegisterCommand
import com.google.inject.Injector
import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.plugin.Plugin
import org.reflections.Reflections

object CommandRegistrar {
    fun registerAll(plugin: Plugin, injector: Injector) {
        val reflections = Reflections("com.github.saintedlittle.commands")
        val commandClasses = reflections.getTypesAnnotatedWith(AutoRegisterCommand::class.java)

        commandClasses.forEach { commandClass ->
            try {
                val instance = injector.getInstance(commandClass)

                if (instance is CommandExecutor) {
                    val commandName = commandClass.getAnnotation(AutoRegisterCommand::class.java).command
                    val pluginCommand = Bukkit.getPluginCommand(commandName)

                    if (pluginCommand != null) {
                        pluginCommand.setExecutor(instance)
                        plugin.logger.info("Successfully registered command: ${commandClass.simpleName} (${commandName})")
                    } else {
                        plugin.logger.warning("Command '$commandName' is not defined in the plugin.yml. Registration skipped.")
                    }
                } else {
                    plugin.logger.warning(
                        "Class ${commandClass.simpleName} is annotated with @AutoRegisterCommand but does not implement CommandExecutor."
                    )
                }
            } catch (e: Exception) {
                plugin.logger.severe("Error registering command ${commandClass.simpleName}: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
