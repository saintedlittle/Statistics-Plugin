package com.github.saintedlittle.commands

import com.github.saintedlittle.annotations.AutoRegisterCommand
import com.github.saintedlittle.application.JsonManager
import com.github.saintedlittle.messaging.KafkaProducerService
import com.google.inject.Inject
import kotlinx.coroutines.CoroutineScope
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.slf4j.Logger

@AutoRegisterCommand
class SynchronizeCommand @Inject constructor(
    private val kafkaProducerService: KafkaProducerService,
    private val jsonManager: JsonManager,
    private val scope: CoroutineScope,
    private val logger: Logger
) : NamedCommand {
    override fun getCommand(): String = "synchronize"

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            if (sender !is Player) {
                sender.sendMessage("This command is only available to players.")
                return true
            }
        }

        return true
    }
}