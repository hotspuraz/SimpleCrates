package com.simplesurvival.crates.menu

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.lib.SimpleLib
import com.simplesurvival.lib.configuration.yaml.YamlBuilder
import com.simplesurvival.lib.util.loader.ClassLoader
import java.util.logging.Level

object MenuHandler
{

    val yamlCache: MutableMap<String, YamlBuilder<*>> = mutableMapOf()

    fun load()
    {
        yamlCache.clear()

        for (menuClass in ClassLoader.getClassesForPackage(SimpleCrates.plugin, "com.simplesurvival.crates.menu"))
        {
            if (YamlBuilder::class.java.isAssignableFrom(menuClass))
            {
                try
                {
                    @Suppress("UNCHECKED_CAST")
                    val yaml = menuClass.getField("INSTANCE").get(null) as? YamlBuilder<*>
                        ?: continue

                    yamlCache[menuClass.canonicalName] = yaml
                    yaml.init()
                } catch (ex: Exception)
                {
                    SimpleLib.getLogger()
                        .log(Level.SEVERE, "An error occurred while loading ${menuClass.canonicalName}", ex)
                }
            }
        }
    }

    fun reload()
    {
        yamlCache.values.forEach { it.reload() }
    }
}


