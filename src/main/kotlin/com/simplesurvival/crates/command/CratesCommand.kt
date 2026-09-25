package com.simplesurvival.crates.command

import com.simplesurvival.crates.command.subs.EditorCommand
import com.simplesurvival.crates.command.subs.GiveCrateCommand
import com.simplesurvival.crates.command.subs.GiveKeyCommand
import com.simplesurvival.crates.command.subs.KeysCommand
import com.simplesurvival.crates.command.subs.ReloadCommand
import com.simplesurvival.crates.command.subs.TakeKeyCommand
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class CratesCommand : CommandExecutor, TabCompleter
{

    private val subCommands: List<SubCommand> = listOf(
        EditorCommand(),
        GiveKeyCommand(),
        GiveCrateCommand(),
        TakeKeyCommand(),
        KeysCommand(),
        ReloadCommand()
    )

    private fun find(name: String): SubCommand? = subCommands.firstOrNull { it.name.equals(name, ignoreCase = true) }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean
    {
        val sub = args.getOrNull(0)?.let(::find)
        if (sub == null)
        {
            sender.sendMessage("§7Usage: /crates <${subCommands.joinToString("|") { it.name }}>")
            return true
        }

        if (!sub.canUse(sender))
        {
            sender.sendMessage("§cYou don't have permission to use this command.")
            return true
        }

        sub.dispatch(sender, args.drop(1))
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String>
    {
        if (args.size <= 1)
        {
            val prefix = args.getOrNull(0) ?: ""
            return subCommands
                .filter { it.canUse(sender) }
                .map { it.name }
                .filter { it.startsWith(prefix, ignoreCase = true) }
        }

        val sub = args.getOrNull(0)?.let(::find) ?: return emptyList()
        if (!sub.canUse(sender)) return emptyList()

        return sub.tabComplete(sender, args.drop(1))
    }
}
