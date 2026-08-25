package com.simplesurvival.crates.service.crate.objects.engine.types

import com.simplesurvival.crates.service.crate.objects.engine.CrateEngine
import com.simplesurvival.crates.service.crate.objects.engine.CrateEngineType
import org.bukkit.Material

class VanillaModel(
    var material: Material = Material.FEATHER,

    var customModelData: Int = 0,
    var rotationDegrees: Int = 0,

    var offSetX: Double = 0.0,
    var offSetY: Double = 0.0,
    var offSetZ: Double = 0.0
) : CrateEngine(CrateEngineType.VANILLA_MODEL)