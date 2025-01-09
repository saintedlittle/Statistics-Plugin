package com.github.saintedlittle.utils

import com.github.saintedlittle.annotations.AutoRegisterCommand
import com.github.saintedlittle.commands.NamedCommand
import com.google.inject.Injector
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.reflections.Reflections

object CommandRegistrar {
    fun registerAll(plugin: Plugin, injector: Injector) {
        val reflections = Reflections("com.github.saintedlittle.commands")
        val commands = reflections.getTypesAnnotatedWith(AutoRegisterCommand::class.java)
        commands.forEach { commandClass ->
            val command = injector.getInstance(commandClass) as NamedCommand
            Bukkit.getPluginCommand(command.getCommand())?.setExecutor(command)
            plugin.logger.info("Registered command: ${commandClass.simpleName}")
        }
    }
}