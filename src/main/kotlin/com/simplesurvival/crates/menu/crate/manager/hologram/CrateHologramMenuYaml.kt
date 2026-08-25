package com.simplesurvival.crates.menu.crate.manager.hologram

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.lib.configuration.key.SimpleKey
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import com.simplesurvival.lib.configuration.yaml.YamlBuilder
import com.simplesurvival.lib.util.caps.SmallCapsConverter.format
import org.bukkit.Material

object CrateHologramMenuYaml : YamlBuilder<SimpleCrates>(
    SimpleCrates.plugin,
    "menus/crate/manager/hologram/crate_hologram.yml"
)
{

    @field:SimpleKey(node = "menu.title")
    var title = "${format("Crate ➡ Hologram ➡")} %id%"

    @field:SimpleKey(node = "menu.rows")
    var rows = 4

    @field:SimpleKey(node = "menu.items.lines")
    var linesItem: SimpleItemBuilder = SimpleItemBuilder(Material.PAPER)
        .withName("&a${format("Lines")}")
        .withLore(
            "&7${format("By clicking here you will be able to")}",
            "&7${format("configure the hologram lines of this crate.")}",
            "",
            "&6&n${format("TIP:")} &7${format("You can use")} &e${format("PlaceholderAPI")}&7.",
            "",
            "&f${format("Hologram Lines:")}",
            "%lines%",
            "",
            "&6${format("Left-click:")} &e${format("Add line.")}",
            "&6${format("Right-click:")} &e${format("Remove last one.")}",
            "&6${format("Shift + Left-click:")} &e${format("Add empty line.")}",
            "&6${format("Shift + Right-click:")} &e${format("Clear lines.")}",
        )
        .withSlots(12)

    @field:SimpleKey(node = "menu.items.offset")
    var offSetItem: SimpleItemBuilder = SimpleItemBuilder(Material.ITEM_FRAME)
        .withName("&a${format("Offset")}")
        .withLore(
            "&7${format("This option allows you to adjust the hologram")}",
            "&7${format("position based on the crate location.")}",
            "",
            "&f${format("Current offset:")} &7%offset%",
            "",
            "&6${format("Left-click:")} &e${format("Add 0.1.")}",
            "&6${format("Right-click:")} &e${format("Remove 0.1.")}",
            "&6${format("Shift + Left-click:")} &e${format("Add 1.")}",
            "&6${format("Shift + Right-click:")} &e${format("Remove 1.")}",
        )
        .withSlots(14)

    @field:SimpleKey(node = "menu.items.back")
    var backItem: SimpleItemBuilder = SimpleItemBuilder(Material.FEATHER)
        .withName("&c${format("Go back")}")
        .withSlots(31)

    @field:SimpleKey(node = "menu.items.filler")
    var fillerItem: SimpleItemBuilder = SimpleItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
        .withName("&7")
        .withSlots(27, 28, 29, 30, 32, 33, 34, 35)
}

