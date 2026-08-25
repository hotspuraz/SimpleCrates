package com.simplesurvival.crates.service.crate.objects.reward

import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import org.bukkit.Material
import java.util.UUID

data class Reward(
    val id: String = UUID.randomUUID().toString(),
    var identifier: String = generateIdentifier(),
    val item: SimpleItemBuilder = SimpleItemBuilder(Material.AIR),
    val winItems: MutableList<SimpleItemBuilder> = mutableListOf(),
    val winCommands: MutableList<String> = mutableListOf(),
    var winLimit: Int = 0,
    var weight: Double = 100.0,
    var broadcastMessageEnabled: Boolean = false,
    val restrictedPermissions: MutableList<String> = mutableListOf()
)
{

    companion object
    {
        private fun generateIdentifier(): String
        {
            return "reward_" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .take(10)
        }
    }
}
