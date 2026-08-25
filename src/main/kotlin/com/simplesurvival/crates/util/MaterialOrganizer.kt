package com.simplesurvival.crates.util

import org.bukkit.Material

object MaterialOrganizer
{
    private val colorOrder = listOf(
        "WHITE", "LIGHT_GRAY", "GRAY", "BLACK",
        "BROWN", "RED", "ORANGE", "YELLOW", "LIME", "GREEN",
        "CYAN", "LIGHT_BLUE", "BLUE", "PURPLE", "MAGENTA", "PINK"
    )

    fun getOrganizedSolidBlocks(): List<Material>
    {
        return Material.entries
            .filter { material ->
                material.isBlock &&
                        material.isSolid &&
                        material.isItem
            }
            .sortedWith(
                compareBy<Material> { groupPriority(it) }
                    .thenBy { familyName(it) }
                    .thenBy { colorPriority(it) }
                    .thenBy { ItemNaming.format(it) }
            )
    }

    private fun groupPriority(material: Material): Int
    {
        val name = material.name

        return when
        {
            name.endsWith("_SHULKER_BOX") || name == "SHULKER_BOX" -> 10

            name.endsWith("_WOOL") -> 20
            name.endsWith("_CONCRETE") -> 21
            name.endsWith("_CONCRETE_POWDER") -> 22
            name.endsWith("_TERRACOTTA") -> 23
            name.endsWith("_GLAZED_TERRACOTTA") -> 24
            name.endsWith("_STAINED_GLASS") -> 25
            name.endsWith("_STAINED_GLASS_PANE") -> 26

            name.endsWith("_PLANKS") -> 30
            name.endsWith("_LOG") -> 31
            name.endsWith("_WOOD") -> 32
            name.endsWith("_LEAVES") -> 33
            name.endsWith("_SAPLING") -> 34

            name.contains("STONE") -> 40
            name.contains("DEEPSLATE") -> 41
            name.contains("COPPER") -> 42

            else -> 999
        }
    }

    private fun familyName(material: Material): String
    {
        return material.name
            .removePrefix(colorPrefix(material))
    }

    private fun colorPriority(material: Material): Int
    {
        val color = colorOrder.firstOrNull { material.name.startsWith("${it}_") }

        return color?.let { colorOrder.indexOf(it) } ?: Int.MAX_VALUE
    }

    private fun colorPrefix(material: Material): String
    {
        val color = colorOrder.firstOrNull { material.name.startsWith("${it}_") }

        return if (color != null) "${color}_" else ""
    }
}