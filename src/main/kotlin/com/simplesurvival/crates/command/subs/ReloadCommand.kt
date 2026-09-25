package com.simplesurvival.crates.command.subs

import com.simplesurvival.crates.command.SubCommand
import com.simplesurvival.crates.service.ServiceHandler
import org.bukkit.command.CommandSender

class ReloadCommand : SubCommand("reload", "simplecrates.admin")
{

    override fun execute(sender: CommandSender, args: List<String>)
    {
        ServiceHandler.reload()

        sender.sendMessage("§aPlugin has been reloaded.")
    }
}
