package com.simplesurvival.crates.service.crate

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.crates.config.MainConfiguration
import com.simplesurvival.lib.hologram.registry.HologramProviderRegistry
import com.simplesurvival.crates.util.DisplayNameFormatUtil
import eu.decentsoftware.holograms.api.DHAPI

object CrateHologramService
{

    private const val PREFIX = "simplecrates_crate_"

    private val crateHolograms: MutableMap<String, MutableSet<String>> = mutableMapOf()

    fun refreshAll()
    {
        CrateService.crates.values.forEach { refresh(it) }
    }

    fun refresh(crate: Crate)
    {
        remove(crate)

        val provider = HologramProviderRegistry.getProvider()

        crate.locations
            .filter { it.isValid() }
            .forEachIndexed { index, crateLocation ->
                val base = crateLocation.hologramBase(crate.hologram.offset) ?: return@forEachIndexed
                val hologramId = hologramId(crate.identifier, index)
                val crateName = DisplayNameFormatUtil.normalize(crate.item.rawDisplayName)

                val lines = crate.hologram.lines
                    .map { line -> line.replace("%name%", crateName) }
                    .toTypedArray()

                if (provider.create(hologramId, base, *lines))
                {
                    applyDecentHologramsLineSpacing(hologramId, lines.toList())
                    crateHolograms.getOrPut(crate.identifier.lowercase()) { mutableSetOf() }.add(hologramId)
                }
            }
    }

    fun remove(crate: Crate)
    {
        val provider = HologramProviderRegistry.getProvider()
        val crateId = crate.identifier.lowercase()
        val holograms = crateHolograms.remove(crateId) ?: return

        holograms.forEach { provider.delete(it) }
    }

    private fun hologramId(crateIdentifier: String, index: Int): String
    {
        return "$PREFIX${crateIdentifier.lowercase()}_$index"
    }

    private fun applyDecentHologramsLineSpacing(hologramId: String, lines: List<String>)
    {
        if (!MainConfiguration.crateHologramTightSpacingEnabled) return
        val decentPlugin = SimpleCrates.plugin.server.pluginManager.getPlugin("DecentHolograms") ?: return
        if (!decentPlugin.isEnabled) return

        runCatching {
            val hologram = DHAPI.getHologram(hologramId) ?: run {
                SimpleCrates.plugin.logger.warning("Could not find DH hologram '$hologramId' to apply spacing.")
                return
            }
            val page = DHAPI.getHologramPage(hologram, 0) ?: run {
                SimpleCrates.plugin.logger.warning("Could not find first page in DH hologram '$hologramId' to apply spacing.")
                return
            }

            val pageLines = page.lines
            var appliedLines = 0

            pageLines.forEachIndexed { index, line ->
                val rawLine = lines.getOrNull(index).orEmpty()
                val isEmptyLine = rawLine.isBlank()
                val height =
                    if (isEmptyLine) MainConfiguration.crateHologramEmptyLineHeight else MainConfiguration.crateHologramTextLineHeight
                line.height = height.coerceAtLeast(0.001)
                appliedLines++
            }

            hologram.realignLines()
            DHAPI.updateHologram(hologramId)
        }.onFailure { ex ->
            SimpleCrates.plugin.logger.warning("Failed to apply DecentHolograms line spacing for '$hologramId': ${ex.message}")
        }
    }

}
