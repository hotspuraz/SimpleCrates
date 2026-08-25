package com.simplesurvival.crates.menu.crate.manager.item

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.lib.configuration.key.SimpleKey
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import com.simplesurvival.lib.configuration.yaml.YamlBuilder
import com.simplesurvival.lib.util.caps.SmallCapsConverter.format
import org.bukkit.Material

object CrateItemMenuYaml : YamlBuilder<SimpleCrates>(
    SimpleCrates.plugin,
    "menus/crate/manager/item/crate_item.yml"
)
{

    @field:SimpleKey(node = "menu.title")
    var title = "${format("Crate ➡ Item ➡")} %id%"

    @field:SimpleKey(node = "menu.rows")
    var rows: Int = 4

    @field:SimpleKey(node = "menu.items.crate-item")
    var crateItem: SimpleItemBuilder = SimpleItemBuilder(Material.CHEST)
        .withName("&a${format("Item")}")
        .withLore(
            "&7${format("Display item that will")}",
            "&7${format("be used to represent this crate.")}",
            "",
            "&f${format("Item:")}",
            " &f${format("Name:")} %name%",
            " &f${format("Lore:")}",
            "%lore%",
            "",
            "&6${format("Drag & Drop:")} &e${format("Replace Item.")}"
        )
        .withSlots(11)

    @field:SimpleKey(node = "menu.items.name")
    var itemName: SimpleItemBuilder = SimpleItemBuilder(Material.PAPER)
        .withName("&a${format("Item Name")}")
        .withLore(
            "&7${format("The name that will be used")}",
            "&7${format("to represent this item.")}",
            "",
            "&f${format("Display Name:")} %name%",
            "",
            "&6${format("Left-click:")} &e${format("Change Display Name.")}"
        )
        .withSlots(13)

    @field:SimpleKey(node = "menu.items.lore")
    var itemLore: SimpleItemBuilder = SimpleItemBuilder(Material.GLOBE_BANNER_PATTERN)
        .withName("&a${format("Item Lore")}")
        .withLore(
            "&7${format("The lore that will be used")}",
            "&7${format("to represent this item.")}",
            "",
            "&f${format("Item Lore:")}",
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

