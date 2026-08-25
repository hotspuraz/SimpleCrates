package com.simplesurvival.crates.command.subs

import org.bukkit.Bukkit
import org.bukkit.entity.Player

internal data class GiveCommandInput(
    val playerName: String,
    val identifier: String,
    val amount: Int
)

internal object GiveCommandSupport
{

    fun parse(input: String?): GiveCommandInput?
    {
        if (input.isNullOrBlank()) return null

        val parts = input.trim().split("\\s+".toRegex())
        if (parts.size !in 2..3) return null

        val amount = when (parts.size)
        {
            2 -> 1
            3 -> parts[2].toIntOrNull()?.takeIf { it > 0 } ?: return null
            else -> return null
        }

        return GiveCommandInput(
            playerName = parts[0],
            identifier = parts[1],
            amount = amount
        )
    }

    fun resolveOnlinePlayer(playerName: String): Player?
    {
        return Bukkit.getPlayerExact(playerName)
            ?: Bukkit.getOnlinePlayers().firstOrNull { it.name.equals(playerName, ignoreCase = true) }
    }

    fun suggestInput(input: String, identifiers: Collection<String>): Array<String>
    {
        val endsWithSpace = input.endsWith(" ")
        val trimmed = input.trim()
        val parts = if (trimmed.isEmpty()) emptyList() else trimmed.split("\\s+".toRegex())

        if (parts.isEmpty())
        {
            return Bukkit.getOnlinePlayers()
                .map(Player::getName)
                .toTypedArray()
        }

        if (parts.size == 1 && !endsWithSpace)
        {
            val playerPrefix = parts[0]

            return Bukkit.getOnlinePlayers()
                .map(Player::getName)
                .filter { it.startsWith(playerPrefix, ignoreCase = true) }
                .toTypedArray()
        }

        if (parts.size == 1)
        {
            val playerName = parts[0]

            return identifiers
                .map { "$playerName $it" }
                .toTypedArray()
        }

        if (parts.size == 2 && !endsWithSpace)
        {
            val playerName = parts[0]
            val identifierPrefix = parts[1]

            return identifiers
                .filter { it.startsWith(identifierPrefix, ignoreCase = true) }
                .map { "$playerName $it" }
                .toTypedArray()
        }

        if (parts.size == 2)
        {
            val base = "${parts[0]} ${parts[1]}"

            return arrayOf(
                "$base 1",
                "$base 32",
                "$base 64"
            )
        }

        if (parts.size == 3 && !endsWithSpace)
        {
            val base = "${parts[0]} ${parts[1]}"

            return listOf("1", "32", "64")
                .filter { it.startsWith(parts[2]) }
                .map { "$base $it" }
                .toTypedArray()
        }

        return emptyArray()
    }
}
