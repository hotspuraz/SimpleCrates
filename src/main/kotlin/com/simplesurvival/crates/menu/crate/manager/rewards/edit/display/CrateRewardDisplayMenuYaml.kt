package com.simplesurvival.crates.menu.crate.manager.rewards.edit.display

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.lib.configuration.key.SimpleKey
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import com.simplesurvival.lib.configuration.yaml.YamlBuilder
import com.simplesurvival.lib.util.caps.SmallCapsConverter.format
import org.bukkit.Material

object CrateRewardDisplayMenuYaml : YamlBuilder<SimpleCrates>(
    SimpleCrates.plugin,
    "menus/crate/manager/rewards/reward_item_edit.yml"
)
{

    @field:SimpleKey(node = "menu.title")
    var title = "${format("Reward ➡ Display ➡")} %id%"

    @field:SimpleKey(node = "menu.rows")
    var rows: Int = 4

    @field:SimpleKey(node = "menu.items.display")
    var display: SimpleItemBuilder = SimpleItemBuilder(Material.IRON_BARS)
        .withName("&a${format("Display Item")}")
        .withLore(
            "&7${format("This item will serve as the display")}",
            "&7${format("representing the reward for this crate.")}",
            "",
            "&f${format("Item:")}",
            " &f${format("Name:")} %name%",
            " &f${format("Lore:")}",
            "%lore%",
            "",
            "&6${format("Drag & Drop:")} &e${format("Replace Item.")}"
        )
        .withSlots(11)

    @field:SimpleKey(node = "menu.items.display-empty")
    var displayEmpty: SimpleItemBuilder = SimpleItemBuilder(Material.BARRIER)
        .withName("&a${format("Display Item")}")
        .withLore(
            "&7${format("This item will serve as the display")}",
            "&7${format("representing the reward for this crate.")}",
            "",
            "&f${format("Item:")} &7${format("Empty")}",
            "",
            "&6${format("Drag & Drop:")} &e${format("Replace Item.")}"
        )
        .withSlots(11)

    @field:SimpleKey(node = "menu.items.name")
    var rewardName: SimpleItemBuilder = SimpleItemBuilder(Material.PAPER)
        .withName("&a${format("Display Name")}")
        .withLore(
            "&7${format("The name that will be used")}",
            "&7${format("to represent this reward.")}",
            "",
            "&f${format("Display Name:")} %name%",
            "",
            "&6${format("Left-click:")} &e${format("Change Display Name.")}"
        )
        .withSlots(13)

    @field:SimpleKey(node = "menu.items.lore")
    var rewardLore: SimpleItemBuilder = SimpleItemBuilder(Material.GLOBE_BANNER_PATTERN)
        .withName("&a${format("Display Lore")}")
        .withLore(
            "&7${format("The lore that will be used")}",
            "&7${format("to represent this reward.")}",
            "",
            "&f${format("Display Lore:")}",
            "%lore%",
            "",
            "&6${format("Left-click:")} &e${format("Add line.")}",
            "&6${format("Right-click:")} &e${format("Remove last one.")}",
            "&6${format("Shift + Right-click:")} &e${format("Clear all.")}"
        )
        .withSlots(15)

    @field:SimpleKey(node = "menu.items.back")
    var backItem: SimpleItemBuilder = SimpleItemBuilder(Material.FEATHER)
        .withName("&c${format("Go back")}")
        .withSlots(31)

    @field:SimpleKey(node = "menu.items.filler")
    var fillerItem: SimpleItemBuilder = SimpleItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
        .withName("&7")
        .withSlots(27, 28, 29, 30, 32, 33, 34, 35)
}

