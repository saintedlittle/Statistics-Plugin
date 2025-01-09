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
        val commands = reflections.getTypesAnnotatedWith(AutoRegisterCommand::class.java)
        commands.forEach { commandClass ->
            val instance = injector.getInstance(commandClass)
            val commandRegister = instance as AutoRegisterCommand
            val commandExecutor = instance as CommandExecutor
            Bukkit.getPluginCommand(commandRegister.command)?.setExecutor(commandExecutor)
            plugin.logger.info("Registered command: ${commandClass.simpleName}")
        }
    }
}