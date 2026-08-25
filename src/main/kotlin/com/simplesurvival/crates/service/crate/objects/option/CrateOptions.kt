package com.simplesurvival.crates.service.crate.objects.option

class CrateOptions(

    var enabled: Boolean = true,
    var previewReward: Boolean = false,

    var openMoneyCost: Double = 0.0,
    var openCooldownInSeconds: Int = 0
)