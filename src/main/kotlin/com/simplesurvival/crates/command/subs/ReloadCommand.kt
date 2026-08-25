package com.simplesurvival.crates.command.subs

import com.simplesurvival.crates.menu.MenuHandler
import com.simplesurvival.crates.service.ServiceHandler
import com.simplesurvival.lib.command.sub.SubCommandAssistance
import dev.jorel.commandapi.executors.CommandArguments
import org.bukkit.command.CommandSender

class ReloadCommand : SubCommandAssistance("reload", "simplecrates.admin", Target.CONSOLE)
{

    override fun handle(sender: CommandSender, args: CommandArguments)
    {

        ServiceHandler.reload()

        sender.sendMessage("§aPlugin has been reloaded.")
    }

}