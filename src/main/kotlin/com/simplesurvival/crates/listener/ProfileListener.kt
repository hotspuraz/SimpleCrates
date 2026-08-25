package com.simplesurvival.crates.listener

import com.simplesurvival.crates.service.profile.CrateProfileService
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

class ProfileListener : Listener
{

    @EventHandler(priority = EventPriority.HIGH)
    fun load(event: AsyncPlayerPreLoginEvent) {

        val uuid = event.uniqueId

        CrateProfileService.instance.load(uuid)
    }
}