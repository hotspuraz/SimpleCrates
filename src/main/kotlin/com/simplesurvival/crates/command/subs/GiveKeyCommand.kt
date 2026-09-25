package com.simplesurvival.crates.command.subs

import com.simplesurvival.crates.command.SubCommand
import com.simplesurvival.crates.config.CommandsConfiguration
import com.simplesurvival.crates.service.key.KeyService
import com.simplesurvival.crates.service.profile.CrateProfileService
import org.bukkit.command.CommandSender

class GiveKeyCommand : SubCommand("givekey", "simplecrates.admin")
{

    override fun execute(sender: CommandSender, args: List<String>)
    {
        val playerName = args.getOrNull(0)
        val identifier = args.getOrNull(1)
        if (playerName == null || identifier == null)
        {
            CommandsConfiguration.giveKeyUsage.send(sender)
            return
        }
        val amount = args.getOrNull(2)?.toIntOrNull()?.takeIf { it > 0 } ?: 1

        val target = GiveCommandSupport.resolveOnlinePlayer(playerName)
            ?: run {
                CommandsConfiguration.giveKeyPlayerNotOnline.send(sender) {
                    it.replace("%player%", playerName)
                }
                return
            }

        val key = KeyService.get(identifier)
        if (key == null)
        {
            CommandsConfiguration.giveKeyNotFound.send(sender) {
                it.replace("%key%", identifier)
            }
            return
        }

        if (key.virtual)
        {
            val crateProfile = CrateProfileService.instance.load(target.uniqueId)
            repeat(amount) {
                crateProfile.addKey(key.identifier)
            }
        } else
        {
            repeat(amount) {
                val keyItem = key.item.cloneBuilder()
                target.inventory.addItem(keyItem).values.forEach { leftover ->
                    target.world.dropItemNaturally(target.location, leftover)
                }
            }

            target.updateInventory()
        }

        CommandsConfiguration.receivedKey.send(target) {
            it
                .replace("%amount%", amount.toString())
                .replace("%key%", identifier)
                .replace("%sender%", sender.name)
        }

        CommandsConfiguration.giveKeySuccess.send(sender) {
            it
                .replace("%amount%", amount.toString())
                .replace("%key%", identifier)
                .replace("%player%", target.name)
        }
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String>
    {
        return when (args.size)
        {
            1 -> GiveCommandSupport.onlinePlayerNames(args[0])
            2 -> KeyService.keyMap.keys.filter { it.startsWith(args[1], ignoreCase = true) }
            3 -> listOf("1", "32", "64").filter { it.startsWith(args[2]) }
            else -> emptyList()
        }
    }
}
