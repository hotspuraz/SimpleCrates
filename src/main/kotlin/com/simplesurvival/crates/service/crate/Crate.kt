package com.simplesurvival.crates.service.crate

import com.simplesurvival.crates.service.crate.objects.engine.CrateEngine
import com.simplesurvival.crates.service.crate.objects.engine.CrateEngineType
import com.simplesurvival.crates.service.crate.objects.engine.types.VanillaBlock
import com.simplesurvival.crates.service.crate.objects.engine.types.VanillaModel
import com.simplesurvival.crates.service.crate.objects.hologram.CrateHologram
import com.simplesurvival.crates.service.crate.objects.key.CrateKey
import com.simplesurvival.crates.service.crate.objects.location.CrateLocation
import com.simplesurvival.crates.service.crate.objects.option.CrateOptions
import com.simplesurvival.crates.service.crate.objects.permission.CratePermission
import com.simplesurvival.crates.service.crate.objects.reward.Reward
import com.simplesurvival.crates.service.key.Key
import com.simplesurvival.crates.service.key.KeyService
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import org.bukkit.Material
import java.util.UUID

class Crate(
    val uuid: UUID,
    var identifier: String,
    var displayName: String,
    var item: SimpleItemBuilder,
    val hologram: CrateHologram = CrateHologram(),
    val permission: CratePermission = CratePermission("simplecrates.crate.${identifier.lowercase()}"),
    val key: CrateKey = CrateKey(),
    val locations: MutableList<CrateLocation> = mutableListOf(),
    val options: CrateOptions = CrateOptions(),
    val rewards: MutableList<Reward> = mutableListOf(),
    var broadcastMessages: MutableList<String> = mutableListOf(),
    var previewRewardSwitchSeconds: Int = 3,
    var previewRewardText: String = "&e%reward%",
    var previewRewardTextHeight: Double = 1.7499999999999996,
    var previewRewardItemHeight: Double = 1.4000000000000001,
    var animationEnabled: Boolean = false,

    val engines: MutableMap<CrateEngineType, CrateEngine> = mutableMapOf(
        CrateEngineType.VANILLA_BLOCK to VanillaBlock(),
        CrateEngineType.VANILLA_MODEL to VanillaModel()
    ),
    var activeEngine: CrateEngineType = CrateEngineType.VANILLA_BLOCK
)
{

    val engine: CrateEngine
        get() = engines[activeEngine] ?: VanillaBlock()

    fun isUsingEngine(type: CrateEngineType) = engine.type == type

    fun switchEngine()
    {

        val next = activeEngine.next()

        if (!engines.containsKey(next))
        {
            engines[next] = when (next)
            {
                CrateEngineType.VANILLA_BLOCK -> VanillaBlock()
                CrateEngineType.VANILLA_MODEL -> VanillaModel()
            }
        }

        this.activeEngine = next
        update()
    }

    fun updateEngine(engine: CrateEngine)
    {
        engines[engine.type] = engine
        CrateService.updatePlacedBlocks(this)
        update()
    }

    fun update() =
        CrateService.updateCrate(this)

    fun delete() = CrateService.deleteCrate(identifier)

    fun clone() = CrateService.cloneCrate(identifier)

    fun linkedKeys(): List<Key>
    {
        return key.ids.mapNotNull { KeyService.get(it) }
    }

    fun unlinkedKeys(): List<Key>
    {
        return KeyService.keyMap.values.filter { key -> !this.key.ids.contains(key.identifier) }
    }

    fun currentBlockMaterial(): Material
    {
        val currentEngine = engines[activeEngine]
        return if (currentEngine is VanillaBlock) currentEngine.material else item.type
    }
}
