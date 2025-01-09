package com.github.saintedlittle.commands

import org.bukkit.command.CommandExecutor

interface NamedCommand : CommandExecutor {
    fun getCommand(): String
}