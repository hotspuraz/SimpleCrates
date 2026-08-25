package com.simplesurvival.crates.command.subs

import com.simplesurvival.crates.menu.editor.EditorMenu
import com.simplesurvival.crates.menu.crate.CratesMenu
import com.simplesurvival.crates.menu.keys.KeysMenu
import com.simplesurvival.lib.command.sub.SubCommandAssistance
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandArguments
import org.bukkit.entity.Player

class EditorCommand : SubCommandAssistance("editor", "simplecrates.admin", Target.PLAYER)
{

    override fun handle(player: Player, args: CommandArguments)
    {

        val typeRaw = args.get("type") as String?

        if (typeRaw.isNullOrBlank())
        {
            EditorMenu(player).build()
            return
        }

        when (typeRaw)
        {
            "crates" -> CratesMenu(player, EditorMenu(player)).build()
            "keys" -> KeysMenu(player, EditorMenu(player)).build()
        }
    }

    override fun optionalArguments(): List<Argument<*>> = listOf(
        StringArgument("type")
            .replaceSuggestions(ArgumentSuggestions.strings("crates", "keys"))
    )
}