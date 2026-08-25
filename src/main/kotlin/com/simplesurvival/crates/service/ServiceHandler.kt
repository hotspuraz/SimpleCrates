package com.simplesurvival.crates.service

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.crates.config.CommandsConfiguration
import com.simplesurvival.crates.config.MainConfiguration
import com.simplesurvival.crates.config.MessagesConfiguration
import com.simplesurvival.crates.menu.MenuHandler
import com.simplesurvival.crates.service.crate.CrateHologramService
import com.simplesurvival.crates.service.crate.CratePreviewRewardService
import com.simplesurvival.crates.service.crate.CrateRewardDisplayService
import com.simplesurvival.crates.service.crate.CrateService
import com.simplesurvival.crates.service.key.KeyService
import com.simplesurvival.crates.service.profile.CrateProfileService
import com.simplesurvival.lib.hologram.registry.HologramProviderRegistry

object ServiceHandler
{

    fun init()
    {
        CrateProfileService.instance

        CrateService.init(SimpleCrates.plugin)
        val restoredOnInit = CrateService.ensureBlocksPlaced()
        KeyService.init(SimpleCrates.plugin)

        MenuHandler.load()

        MainConfiguration.init()
        MessagesConfiguration.init()
        CommandsConfiguration.init()

        // Hologram Provider
        HologramProviderRegistry.handle(MainConfiguration.hologramProvider)
        CrateRewardDisplayService.clearAll()
        CrateHologramService.refreshAll()
        CratePreviewRewardService.refreshAll()

        if (restoredOnInit > 0)
        {
            SimpleCrates.plugin.logger.info { "Restored $restoredOnInit crate block(s) in the world." }
        }
    }

    fun reload()
    {
        CrateService.init(SimpleCrates.plugin)
        val restoredOnReload = CrateService.ensureBlocksPlaced()
        KeyService.init(SimpleCrates.plugin)

        MenuHandler.reload()

        MainConfiguration.reload()
        MessagesConfiguration.reload()
        CommandsConfiguration.reload()

        // Hologram Provider
        HologramProviderRegistry.handle(MainConfiguration.hologramProvider)
        CrateRewardDisplayService.clearAll()
        CrateHologramService.refreshAll()
        CratePreviewRewardService.refreshAll()

        if (restoredOnReload > 0)
        {
            SimpleCrates.plugin.logger.info { "Restored $restoredOnReload crate block(s) in the world." }
        }
    }

    fun shutdown()
    {
        CratePreviewRewardService.clearAll()
        CrateRewardDisplayService.clearAll()
    }
}
