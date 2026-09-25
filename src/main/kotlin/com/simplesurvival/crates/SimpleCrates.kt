package com.simplesurvival.crates

import com.simplesurvival.crates.command.CratesCommand
import com.simplesurvival.crates.placeholder.SimpleCratesPlaceholderExpansion
import com.simplesurvival.crates.service.ServiceHandler
import com.simplesurvival.lib.SimpleLib
import com.simplesurvival.lib.listener.ListenerHandler
import com.simplesurvival.lib.listener.types.MenuListener
import org.bukkit.plugin.java.JavaPlugin

class SimpleCrates : JavaPlugin()
{

    companion object
    {
        lateinit var plugin: SimpleCrates
    }

    override fun onLoad()
    {
        plugin = this

        SimpleLib.handle(this, true)
    }

    override fun onEnable()
    {

        SimpleLib.enable()

        server.pluginManager.registerEvents(MenuListener(), this);

        ServiceHandler.init()

        val cratesCommand = CratesCommand()
        getCommand("crates")?.apply {
            setExecutor(cratesCommand)
            tabCompleter = cratesCommand
        }

        ListenerHandler(this).handle("com.simplesurvival.crates.listener")

        if (server.pluginManager.isPluginEnabled("PlaceholderAPI"))
        {
            SimpleCratesPlaceholderExpansion().register()
        }

        logger.info { "Plugin has been enabled. Developed by Cássio Martim for Simple Survival (${pluginMeta.version})" }
    }

    override fun onDisable()
    {
        ServiceHandler.shutdown()
        SimpleLib.disable()
    }
}
