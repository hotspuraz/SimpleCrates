package com.simplesurvival.crates.menu.editor

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.lib.configuration.key.SimpleKey
import com.simplesurvival.lib.configuration.serializer.ConfigSerializerRegistry
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import com.simplesurvival.lib.configuration.yaml.YamlBuilder
import com.simplesurvival.lib.util.caps.SmallCapsConverter
import org.bukkit.configuration.MemorySection

object EditorMenuYaml : YamlBuilder<SimpleCrates>(
    SimpleCrates.plugin,
    "menus/editor.yml"
)
{

    @field:SimpleKey(node = "menu.title")
    var title: String = SmallCapsConverter.format("Crates - Editor")

    @field:SimpleKey(node = "menu.rows")
    var rows: Int = 4

    @field:SimpleKey(node = "menu.items")
    var items: Map<String, Any> = emptyMap()

    fun items(): Map<String, SimpleItemBuilder>
    {
        val serializer = ConfigSerializerRegistry.read(SimpleItemBuilder::class.java)
            ?: return emptyMap()

        return items.mapNotNull { (id, raw) ->
            val map = when (raw)
            {
                is MemorySection -> raw.getValues(false)
                is Map<*, *> -> raw.entries.associate { it.key.toString() to it.value }
                else -> return@mapNotNull null
            }

            id to serializer.deserializeComplex(map)
        }.toMap()
    }
}

