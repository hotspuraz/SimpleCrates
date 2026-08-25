package com.simplesurvival.crates.placeholder

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.crates.service.profile.CrateProfileService
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

class SimpleCratesPlaceholderExpansion : PlaceholderExpansion()
{

    override fun getIdentifier(): String = "simplecrates"

    override fun getAuthor(): String = "Cássio Martim"

    override fun getVersion(): String = SimpleCrates.plugin.pluginMeta.version

    override fun persist(): Boolean = true

    override fun onPlaceholderRequest(player: Player?, params: String): String
    {
        val normalized = params.lowercase()

        if (normalized == "keys")
        {
            if (player == null) return "0"
            val profile = CrateProfileService.instance.load(player.uniqueId)
            return profile.keys().values.sum().toString()
        }

        if (normalized.startsWith("keys_"))
        {
            val targetName = params.removePrefix("keys_").trim()
            if (targetName.isBlank()) return "0"

            val offline: OfflinePlayer = Bukkit.getOfflinePlayer(targetName)
            val profile = CrateProfileService.instance.load(offline.uniqueId)
            return profile.keys().values.sum().toString()
        }

        if (normalized.startsWith("key_"))
        {
            if (player == null) return "0"
            val profile = CrateProfileService.instance.load(player.uniqueId)
            val keyId = normalized.removePrefix("key_").trim()
            if (keyId.isBlank()) return "0"
            return profile.getKey(keyId).toString()
        }

        return ""
    }
}
