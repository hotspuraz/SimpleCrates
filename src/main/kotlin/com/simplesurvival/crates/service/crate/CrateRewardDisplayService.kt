package com.simplesurvival.crates.service.crate

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity

object CrateRewardDisplayService
{
    private const val TAG_PREFIX = "simplecrates_reward_display_"

    fun buildTag(crateId: String, location: Location): String
    {
        return TAG_PREFIX + crateId.lowercase() + "_" +
                location.blockX + "_" +
                location.blockY + "_" +
                location.blockZ
    }

    fun remove(tag: String)
    {
        Bukkit.getWorlds().forEach { world ->
            world.entities
                .filter { entity -> entity.scoreboardTags.contains(tag) }
                .forEach(Entity::remove)
        }
    }

    fun clearAll()
    {
        Bukkit.getWorlds().forEach { world ->
            world.entities
                .filter { entity -> entity.scoreboardTags.any { it.startsWith(TAG_PREFIX) } }
                .forEach(Entity::remove)
        }
    }
}
