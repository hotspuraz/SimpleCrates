package com.simplesurvival.crates.service.crate.objects.engine.types

import com.simplesurvival.crates.service.crate.objects.engine.CrateEngine
import com.simplesurvival.crates.service.crate.objects.engine.CrateEngineType
import org.bukkit.Material

class VanillaBlock(
    var material: Material = Material.ENDER_CHEST
) : CrateEngine(CrateEngineType.VANILLA_BLOCK)
