package com.simplesurvival.crates.command.subs

import com.simplesurvival.crates.command.SubCommand
import com.simplesurvival.crates.menu.crate.CratesMenu
import com.simplesurvival.crates.menu.editor.EditorMenu
import com.simplesurvival.crates.menu.keys.KeysMenu
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class EditorCommand : SubCommand("editor", "simplecrates.admin", Target.PLAYER)
{

    override fun execute(player: Player, args: List<String>)
    {
        val type = args.getOrNull(0)

        if (type.isNullOrBlank())
        {
            EditorMenu(player).build()
            return
        }

        when (type)
        {
            "crates" -> CratesMenu(player, EditorMenu(player)).build()
            "keys" -> KeysMenu(player, EditorMenu(player)).build()
        }
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String>
    {
        if (args.size != 1) return emptyList()
        return listOf("crates", "keys").filter { it.startsWith(args[0], ignoreCase = true) }
    }
}
