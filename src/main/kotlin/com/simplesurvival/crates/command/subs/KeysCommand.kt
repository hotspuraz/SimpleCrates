package com.simplesurvival.crates.command.subs

import com.simplesurvival.crates.config.CommandsConfiguration
import com.simplesurvival.crates.service.key.KeyService
import com.simplesurvival.crates.service.profile.CrateProfileService
import com.simplesurvival.lib.command.sub.SubCommandAssistance
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandArguments
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class KeysCommand : SubCommandAssistance("keys", "simplecrates.admin", Target.CONSOLE)
{

    override fun handle(sender: CommandSender, args: CommandArguments)
    {
        val playerName = args.get("player") as String? ?: run {
            CommandsConfiguration.keysUsage.send(sender)
            return
        }
        val keyId = args.get("key") as String?
        val onlineTargetByName = Bukkit.getPlayerExact(playerName)
        val uniqueId = onlineTargetByName?.uniqueId ?: Bukkit.getOfflinePlayer(playerName).uniqueId
        val targetName = onlineTargetByName?.name ?: Bukkit.getOfflinePlayer(uniqueId).name ?: playerName
        val profile = CrateProfileService.instance.load(uniqueId)

        if (keyId.isNullOrBlank())
        {
            val total = profile.keys().values.sum()
            CommandsConfiguration.keysTotal.send(sender) {
                it.replace("%player%", targetName)
                    .replace("%amount%", total.toString())
            }
            return
        }

        val key = KeyService.get(keyId) ?: run {
            CommandsConfiguration.giveKeyNotFound.send(sender) {
                it.replace("%key%", keyId)
            }
            return
        }

        if (key.virtual)
        {
            val amount = profile.getKey(keyId)
            CommandsConfiguration.keysSpecific.send(sender) {
                it.replace("%player%", targetName)
                    .replace("%amount%", amount.toString())
                    .replace("%key%", keyId)
            }
            return
        }

        val onlineTarget: Player = onlineTargetByName ?: Bukkit.getPlayer(uniqueId) ?: run {
            CommandsConfiguration.keysPlayerNotOnline.send(sender) {
                it.replace("%player%", targetName)
            }
            return
        }

        var amount = 0
        onlineTarget.inventory.contents.forEach { stack ->
            if (stack != null && stack.isSimilar(key.item))
            {
                amount += stack.amount
            }
        }

        CommandsConfiguration.keysSpecific.send(sender) {
            it.replace("%player%", targetName)
                .replace("%amount%", amount.toString())
                .replace("%key%", keyId)
        }
    }

    override fun optionalArguments(): List<Argument<*>> = listOf(
        StringArgument("player"),
        StringArgument("key").replaceSuggestions(
            ArgumentSuggestions.stringCollection { KeyService.keyMap.keys }
        )
    )
}
