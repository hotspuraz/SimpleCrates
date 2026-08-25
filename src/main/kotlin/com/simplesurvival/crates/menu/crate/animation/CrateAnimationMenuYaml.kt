package com.simplesurvival.crates.menu.crate.animation

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.lib.configuration.key.SimpleKey
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import com.simplesurvival.lib.configuration.yaml.YamlBuilder
import com.simplesurvival.lib.util.caps.SmallCapsConverter.format
import org.bukkit.Material

object CrateAnimationMenuYaml : YamlBuilder<SimpleCrates>(
    SimpleCrates.plugin,
    "menus/crate/animation/open_animation.yml"
)
{

    @field:SimpleKey(node = "animation.title")
    var title: String = "&8${format("Opening")} &6%crate%"

    @field:SimpleKey(node = "animation.rows")
    var rows: Int = 3

    @field:SimpleKey(node = "animation.duration-ticks")
    var durationTicks: Int = 24

    @field:SimpleKey(node = "animation.dramatic-pause-ticks")
    var dramaticPauseTicks: Long = 10L

    @field:SimpleKey(node = "animation.frame-period-ticks")
    var framePeriodTicks: Long = 2L

    @field:SimpleKey(node = "animation.middle-slots")
    var middleSlots: MutableList<Int> = mutableListOf(9, 10, 11, 12, 13, 14, 15, 16, 17)

    @field:SimpleKey(node = "animation.glass-materials")
    var glassMaterials: MutableList<String> = mutableListOf(
        "LIGHT_BLUE_STAINED_GLASS_PANE",
        "CYAN_STAINED_GLASS_PANE",
        "YELLOW_STAINED_GLASS_PANE",
        "LIME_STAINED_GLASS_PANE"
    )

    @field:SimpleKey(node = "animation.reward-lore")
    var rewardLore: MutableList<String> = mutableListOf(
        "&7${format("Potential Reward")}",
        "",
        "&f${format("Chance:")} &e%chance%%"
    )

    @field:SimpleKey(node = "animation.open-sound")
    var openSound: String = "BLOCK_ENDER_CHEST_OPEN"

    @field:SimpleKey(node = "animation.open-volume")
    var openVolume: Double = 1.0

    @field:SimpleKey(node = "animation.open-pitch")
    var openPitch: Double = 1.0

    @field:SimpleKey(node = "animation.tick-sound")
    var tickSound: String = "UI_BUTTON_CLICK"

    @field:SimpleKey(node = "animation.tick-volume")
    var tickVolume: Double = 0.6

    @field:SimpleKey(node = "animation.tick-pitch")
    var tickPitch: Double = 1.4

    @field:SimpleKey(node = "animation.finish-sound")
    var finishSound: String = "ENTITY_PLAYER_LEVELUP"

    @field:SimpleKey(node = "animation.finish-volume")
    var finishVolume: Double = 1.0

    @field:SimpleKey(node = "animation.finish-pitch")
    var finishPitch: Double = 1.0

    @field:SimpleKey(node = "animation.filler")
    var filler: SimpleItemBuilder = SimpleItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
        .withName("&7")
}
