package com.simplesurvival.crates.command.subs

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.crates.config.CommandsConfiguration
import com.simplesurvival.crates.service.crate.CrateService
import com.simplesurvival.lib.command.sub.SubCommandAssistance
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.GreedyStringArgument
import dev.jorel.commandapi.executors.CommandArguments
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.persistence.PersistentDataType

class GiveCrateCommand : SubCommandAssistance("givecrate", "simplecrates.admin", Target.CONSOLE)
{

    override fun handle(sender: CommandSender, args: CommandArguments)
    {
        val input = GiveCommandSupport.parse(args.get("input") as String?) ?: run {
            CommandsConfiguration.giveCrateUsage.send(sender)
            return
        }

        val target = GiveCommandSupport.resolveOnlinePlayer(input.playerName)
        if (target == null)
        {
            CommandsConfiguration.giveCratePlayerNotOnline.send(sender) {
                it.replace("%player%", input.playerName)
            }
            return
        }

        val crate = CrateService.get(input.identifier)
        if (crate == null)
        {
            CommandsConfiguration.giveCrateNotFound.send(sender) {
                it.replace("%crate%", input.identifier)
            }
            return
        }

        repeat(input.amount) {
            val crateItem = crate.item.cloneBuilder()
            crateItem.type = crate.currentBlockMaterial()
            crateItem.withPersistentDatas(
                SimpleItemBuilder.SimplePersistentData(
                    NamespacedKey(SimpleCrates.plugin, "crate-id"),
                    PersistentDataType.STRING,
                    crate.identifier
                )
            )

            target.inventory.addItem(crateItem).values.forEach { leftover ->
                target.world.dropItemNaturally(target.location, leftover)
            }
        }

        target.updateInventory()
        CommandsConfiguration.giveCrateSuccess.send(sender) {
            it.replace("%amount%", input.amount.toString())
                .replace("%crate%", input.identifier)
                .replace("%player%", target.name)
        }
    }

    override fun optionalArguments(): List<Argument<*>> = listOf(
        GreedyStringArgument("input").replaceSuggestions(
            ArgumentSuggestions.strings { info ->
                GiveCommandSupport.suggestInput(info.currentArg(), CrateService.crates.keys)
            }
        )
    )
}
