package com.simplesurvival.crates.command.subs

import com.simplesurvival.crates.config.CommandsConfiguration
import com.simplesurvival.crates.service.key.KeyService
import com.simplesurvival.crates.service.profile.CrateProfileService
import com.simplesurvival.lib.command.sub.SubCommandAssistance
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandArguments
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TakeKeyCommand : SubCommandAssistance("takekey", "simplecrates.admin", Target.CONSOLE)
{

    override fun handle(sender: CommandSender, args: CommandArguments)
    {
        val playerName = args.get("player") as String? ?: run {
            CommandsConfiguration.takeKeyUsage.send(sender)
            return
        }
        val keyId = args.get("key") as String? ?: run {
            CommandsConfiguration.takeKeyUsage.send(sender)
            return
        }
        val amount = (args.get("amount") as Int?)?.coerceAtLeast(1) ?: 1
        val onlineTargetByName = Bukkit.getPlayerExact(playerName)
        val uniqueId = onlineTargetByName?.uniqueId ?: Bukkit.getOfflinePlayer(playerName).uniqueId
        val targetName = onlineTargetByName?.name ?: Bukkit.getOfflinePlayer(uniqueId).name ?: playerName
        val key = KeyService.get(keyId)
        if (key == null)
        {
            CommandsConfiguration.giveKeyNotFound.send(sender) {
                it.replace("%key%", keyId)
            }
            return
        }

        if (key.virtual)
        {
            val profile = CrateProfileService.instance.load(uniqueId)
            val current = profile.getKey(keyId)

            if (current <= 0)
            {
                CommandsConfiguration.takeKeyPlayerHasNoKey.send(sender) {
                    it.replace("%player%", targetName)
                        .replace("%key%", keyId)
                }
                return
            }

            val toRemove = amount.coerceAtMost(current)
            profile.removeKey(keyId, toRemove)
            val left = current - toRemove

            CommandsConfiguration.takeKeySuccess.send(sender) {
                it.replace("%removed%", toRemove.toString())
                    .replace("%key%", keyId)
                    .replace("%player%", targetName)
                    .replace("%remaining%", left.toString())
            }
            return
        }

        val target: Player = onlineTargetByName ?: Bukkit.getPlayer(uniqueId) ?: run {
            CommandsConfiguration.takeKeyPlayerNotOnline.send(sender) {
                it.replace("%player%", targetName)
            }
            return
        }

        var current = 0
        target.inventory.contents.forEach { stack ->
            if (stack != null && stack.isSimilar(key.item))
            {
                current += stack.amount
            }
        }

        if (current <= 0)
        {
            CommandsConfiguration.takeKeyPlayerHasNoKey.send(sender) {
                it.replace("%player%", targetName)
                    .replace("%key%", keyId)
            }
            return
        }

        var toRemove = amount.coerceAtMost(current)
        target.inventory.contents.forEachIndexed { index, stack ->
            if (toRemove <= 0) return@forEachIndexed
            if (stack == null || !stack.isSimilar(key.item)) return@forEachIndexed

            if (stack.amount <= toRemove)
            {
                toRemove -= stack.amount
                target.inventory.setItem(index, null)
            } else
            {
                stack.amount -= toRemove
                target.inventory.setItem(index, stack)
                toRemove = 0
            }
        }

        target.updateInventory()
        val removed = amount.coerceAtMost(current)
        val left = (current - removed).coerceAtLeast(0)

        CommandsConfiguration.takeKeySuccess.send(sender) {
            it.replace("%removed%", removed.toString())
                .replace("%key%", keyId)
                .replace("%player%", targetName)
                .replace("%remaining%", left.toString())
        }
    }

    override fun optionalArguments(): List<Argument<*>> = listOf(
        StringArgument("player"),
        StringArgument("key").replaceSuggestions(
            ArgumentSuggestions.stringCollection { KeyService.keyMap.keys }
        ),
        IntegerArgument("amount", 1)
    )
}
