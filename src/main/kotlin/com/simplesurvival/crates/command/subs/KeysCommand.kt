package com.simplesurvival.crates.command.subs

import com.simplesurvival.crates.command.SubCommand
import com.simplesurvival.crates.config.CommandsConfiguration
import com.simplesurvival.crates.service.key.KeyService
import com.simplesurvival.crates.service.profile.CrateProfileService
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class KeysCommand : SubCommand("keys", "simplecrates.admin")
{

    override fun execute(sender: CommandSender, args: List<String>)
    {
        val playerName = args.getOrNull(0) ?: run {
            CommandsConfiguration.keysUsage.send(sender)
            return
        }
        val keyId = args.getOrNull(1)
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

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String>
    {
        return when (args.size)
        {
            1 -> GiveCommandSupport.onlinePlayerNames(args[0])
            2 -> KeyService.keyMap.keys.filter { it.startsWith(args[1], ignoreCase = true) }
            else -> emptyList()
        }
    }
}
