package com.simplesurvival.crates.config

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.lib.configuration.key.SimpleKey
import com.simplesurvival.lib.configuration.yaml.YamlBuilder

object MainConfiguration : YamlBuilder<SimpleCrates>(SimpleCrates.plugin, "config.yml")
{

    @field:SimpleKey(node = "hologram-provider")
    var hologramProvider: String = "decent"

    @field:SimpleKey(node = "crate-hologram.tight-spacing.enabled")
    var crateHologramTightSpacingEnabled: Boolean = true

    @field:SimpleKey(node = "crate-hologram.tight-spacing.text-line-height")
    var crateHologramTextLineHeight: Double = 0.28

    @field:SimpleKey(node = "crate-hologram.tight-spacing.empty-line-height")
    var crateHologramEmptyLineHeight: Double = 0.1

    @field:SimpleKey(node = "crate-open.no-key-pushback.enabled")
    var crateOpenNoKeyPushbackEnabled: Boolean = true

    @field:SimpleKey(node = "crate-open.no-key-pushback.horizontal-strength")
    var crateOpenNoKeyPushbackHorizontalStrength: Double = 0.45

    @field:SimpleKey(node = "crate-open.no-key-pushback.vertical-strength")
    var crateOpenNoKeyPushbackVerticalStrength: Double = 0.12

    @field:SimpleKey(node = "crate-open.no-key-pushback.sound")
    var crateOpenNoKeyPushbackSound: String = "entity.player.attack.knockback"

    @field:SimpleKey(node = "crate-open.no-key-pushback.sound-volume")
    var crateOpenNoKeyPushbackSoundVolume: Double = 0.9

    @field:SimpleKey(node = "crate-open.no-key-pushback.sound-pitch")
    var crateOpenNoKeyPushbackSoundPitch: Double = 1.05
}
