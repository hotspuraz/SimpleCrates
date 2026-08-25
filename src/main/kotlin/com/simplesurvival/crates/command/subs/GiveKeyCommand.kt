package com.simplesurvival.crates.command.subs

import com.simplesurvival.crates.config.CommandsConfiguration
import com.simplesurvival.crates.service.key.KeyService
import com.simplesurvival.crates.service.profile.CrateProfileService
import com.simplesurvival.lib.command.sub.SubCommandAssistance
import dev.jorel.commandapi.arguments.*
import dev.jorel.commandapi.executors.CommandArguments
import org.bukkit.command.CommandSender

class GiveKeyCommand : SubCommandAssistance("givekey", "simplecrates.admin", Target.CONSOLE)
{

    override fun handle(sender: CommandSender, args: CommandArguments)
    {
        val input = GiveCommandSupport.parse(args.get("input") as String?) ?: run {
            CommandsConfiguration.giveKeyUsage.send(sender)
            return
        }
        val target = GiveCommandSupport.resolveOnlinePlayer(input.playerName)
            ?: run {
                CommandsConfiguration.giveKeyPlayerNotOnline.send(sender) {
                    it.replace("%player%", input.playerName)
                }
                return
            }

        val key = KeyService.get(input.identifier)
        if (key == null)
        {
            CommandsConfiguration.giveKeyNotFound.send(sender) {
                it.replace("%key%", input.identifier)
            }
            return
        }

        if (key.virtual)
        {
            val crateProfile = CrateProfileService.instance.load(target.uniqueId)
            repeat(input.amount) {
                crateProfile.addKey(key.identifier)
            }
        } else
        {
            repeat(input.amount) {
                val keyItem = key.item.cloneBuilder()
                target.inventory.addItem(keyItem).values.forEach { leftover ->
                    target.world.dropItemNaturally(target.location, leftover)
                }
            }

            target.updateInventory()
        }

        CommandsConfiguration.receivedKey.send(target) {
            it
                .replace("%amount%", input.amount.toString())
                .replace("%key%", input.identifier)
                .replace("%sender%", sender.name)
        }

        CommandsConfiguration.giveKeySuccess.send(sender) {
            it
                .replace("%amount%", input.amount.toString())
                .replace("%key%", input.identifier)
                .replace("%player%", target.name)
        }
    }

    override fun optionalArguments(): List<Argument<*>> = listOf(
        GreedyStringArgument("input").replaceSuggestions(
            ArgumentSuggestions.strings { info ->
                GiveCommandSupport.suggestInput(info.currentArg(), KeyService.keyMap.keys)
            }
        )
    )
}
