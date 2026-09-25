package com.simplesurvival.crates.command.subs

import org.bukkit.Bukkit
import org.bukkit.entity.Player

internal object GiveCommandSupport
{

    fun resolveOnlinePlayer(playerName: String): Player?
    {
        return Bukkit.getPlayerExact(playerName)
            ?: Bukkit.getOnlinePlayers().firstOrNull { it.name.equals(playerName, ignoreCase = true) }
    }

    fun onlinePlayerNames(prefix: String): List<String>
    {
        return Bukkit.getOnlinePlayers()
            .map(Player::getName)
            .filter { it.startsWith(prefix, ignoreCase = true) }
    }
}
