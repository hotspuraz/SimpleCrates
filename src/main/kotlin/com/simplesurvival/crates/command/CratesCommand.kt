package com.simplesurvival.crates.command

import com.simplesurvival.crates.command.subs.EditorCommand
import com.simplesurvival.crates.command.subs.GiveCrateCommand
import com.simplesurvival.crates.command.subs.GiveKeyCommand
import com.simplesurvival.crates.command.subs.KeysCommand
import com.simplesurvival.crates.command.subs.ReloadCommand
import com.simplesurvival.crates.command.subs.TakeKeyCommand
import com.simplesurvival.lib.command.CommandAssistance
import com.simplesurvival.lib.command.sub.SubCommandAssistance
import dev.jorel.commandapi.executors.CommandArguments
import org.bukkit.command.CommandSender

class CratesCommand : CommandAssistance("crates")
{

    override fun handle(sender: CommandSender, args: CommandArguments)
    {

    }

    override fun subCommands(): List<SubCommandAssistance> = listOf(
        EditorCommand(),
        GiveKeyCommand(),
        GiveCrateCommand(),
        TakeKeyCommand(),
        KeysCommand(),
        ReloadCommand()
    )
}
