package com.simplesurvival.crates.util

import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import org.bukkit.Material

object ItemNaming
{
    fun format(material: Material): String
    {
        return material.name
            .lowercase()
            .split("_")
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
    }

    fun displayNameOrMaterial(rawName: String, material: Material): String
    {
        val normalized = DisplayNameFormatUtil.normalize(rawName).trim()
        if (normalized.isNotBlank())
        {
            return normalized
        }

        return format(material)
    }

    fun displayNameOrMaterial(item: SimpleItemBuilder): String
    {
        return displayNameOrMaterial(item.rawDisplayName, item.type)
    }
}
