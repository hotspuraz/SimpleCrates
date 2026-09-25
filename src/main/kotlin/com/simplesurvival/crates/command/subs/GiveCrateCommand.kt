package com.simplesurvival.crates.command.subs

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.crates.command.SubCommand
import com.simplesurvival.crates.config.CommandsConfiguration
import com.simplesurvival.crates.service.crate.CrateService
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.persistence.PersistentDataType

class GiveCrateCommand : SubCommand("givecrate", "simplecrates.admin")
{

    override fun execute(sender: CommandSender, args: List<String>)
    {
        val playerName = args.getOrNull(0)
        val identifier = args.getOrNull(1)
        if (playerName == null || identifier == null)
        {
            CommandsConfiguration.giveCrateUsage.send(sender)
            return
        }
        val amount = args.getOrNull(2)?.toIntOrNull()?.takeIf { it > 0 } ?: 1

        val target = GiveCommandSupport.resolveOnlinePlayer(playerName)
        if (target == null)
        {
            CommandsConfiguration.giveCratePlayerNotOnline.send(sender) {
                it.replace("%player%", playerName)
            }
            return
        }

        val crate = CrateService.get(identifier)
        if (crate == null)
        {
            CommandsConfiguration.giveCrateNotFound.send(sender) {
                it.replace("%crate%", identifier)
            }
            return
        }

        repeat(amount) {
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
            it.replace("%amount%", amount.toString())
                .replace("%crate%", identifier)
                .replace("%player%", target.name)
        }
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String>
    {
        return when (args.size)
        {
            1 -> GiveCommandSupport.onlinePlayerNames(args[0])
            2 -> CrateService.crates.keys.filter { it.startsWith(args[1], ignoreCase = true) }
            3 -> listOf("1", "32", "64").filter { it.startsWith(args[2]) }
            else -> emptyList()
        }
    }
}
