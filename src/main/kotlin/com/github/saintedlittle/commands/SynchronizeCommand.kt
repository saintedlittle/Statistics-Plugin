package com.github.saintedlittle.commands

import com.github.saintedlittle.annotations.AutoRegisterCommand
import com.github.saintedlittle.application.JsonManager
import com.github.saintedlittle.messaging.KafkaProducerService
import com.google.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.slf4j.Logger

@AutoRegisterCommand("synchronize")
class SynchronizeCommand @Inject constructor(
    private val kafkaProducerService: KafkaProducerService,
    private val jsonManager: JsonManager,
    private val scope: CoroutineScope,
    private val logger: Logger
) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            if (sender !is Player) {
                sender.sendMessage("This command is only available to players.")
                return true
            }

            scope.launch {
                try {
                    val playerJson = jsonManager.createPlayerJson(sender)
                    kafkaProducerService.sendPlayerSync(sender.uniqueId.toString(), playerJson)
                    logger.debug("Synchronized data for player ${sender.name}: $playerJson")
                    sender.sendMessage("Data synchronized successfully.")
                } catch (e: Exception) {
                    logger.error("Error during synchronize for ${sender.name}: ${e.message}", e)
                    sender.sendMessage("An error occurred during synchronization.")
                }
            }
        } else {
            if (!sender.hasPermission("statistics.synchronize.other")) {
                sender.sendMessage("You do not have permission to use this command.")
                return true
            }

            val playerName = args[0]
            val onlinePlayer = Bukkit.getPlayerExact(playerName)

            if (onlinePlayer == null || !onlinePlayer.isOnline) {
                sender.sendMessage("This player is not online or does not exist.")
                return true
            }

            scope.launch {
                try {
                    val playerJson = jsonManager.createPlayerJson(onlinePlayer)
                    kafkaProducerService.sendPlayerSync(onlinePlayer.uniqueId.toString(), playerJson)
                    logger.debug("Synchronized data for player ${onlinePlayer.name}: $playerJson")
                    sender.sendMessage("Data synchronized successfully for player ${onlinePlayer.name}.")
                } catch (e: Exception) {
                    logger.error("Error during synchronize for ${onlinePlayer.name}: ${e.message}", e)
                    sender.sendMessage("An error occurred during synchronization for player ${onlinePlayer.name}.")
                }
            }
        }

        return true
    }
}