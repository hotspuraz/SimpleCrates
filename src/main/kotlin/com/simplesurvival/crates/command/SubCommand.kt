package com.simplesurvival.crates.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

abstract class SubCommand(
    val name: String,
    val permission: String? = null,
    val target: Target = Target.CONSOLE
)
{

    enum class Target
    {
        CONSOLE, PLAYER
    }

    fun canUse(sender: CommandSender): Boolean = permission == null || sender.hasPermission(permission)

    fun dispatch(sender: CommandSender, args: List<String>)
    {
        if (target == Target.PLAYER)
        {
            val player = sender as? Player ?: run {
                sender.sendMessage("§cThis command can only be used by a player.")
                return
            }
            execute(player, args)
            return
        }

        execute(sender, args)
    }

    open fun execute(sender: CommandSender, args: List<String>)
    {
    }

    open fun execute(player: Player, args: List<String>)
    {
    }

    open fun tabComplete(sender: CommandSender, args: List<String>): List<String> = emptyList()
}
