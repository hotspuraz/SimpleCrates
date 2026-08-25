package com.simplesurvival.crates.menu.crate

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.lib.configuration.key.SimpleKey
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import com.simplesurvival.lib.configuration.yaml.YamlBuilder
import com.simplesurvival.lib.util.caps.SmallCapsConverter.format;
import org.bukkit.Material

object CratesMenuYaml : YamlBuilder<SimpleCrates>(
    SimpleCrates.plugin,
    "menus/crate/crates.yml"
)
{

    @field:SimpleKey(node = "menu.title")
    var title: String = format("Editor ➡ Crates")

    @field:SimpleKey(node = "menu.rows")
    var rows: Int = 6

    @field:SimpleKey(node = "menu.items-per-page")
    var itemsPerPage: Int = 20

    @field:SimpleKey(node = "menu.start-items-page-slot")
    var startItemsPageSlot: Int = 10

    @field:SimpleKey(node = "menu.items.crate")
    var crateItem: SimpleItemBuilder = SimpleItemBuilder(Material.CHEST)
        .withName(
            "%name% &7(${format("Id:")} %id%)"
        )
        .withLore(
            "&7${format("By clicking here you will be")}",
            "&7${format("able to configure this crate.")}",
            "",
            "&f${format("Enabled:")} %is_enabled%",
            "",
            "&6${format("Left-click:")} &e${format("Navigate.")}",
            "&6${format("Right-click:")} &e${format("Receive crate.")}",
        )

    @field:SimpleKey(node = "menu.items.add-crate")
    var addCrateItem: SimpleItemBuilder = SimpleItemBuilder(Material.LIME_STAINED_GLASS_PANE)
        .withName("&a${format("Add crate")}")
        .withLore(
            "&7${format("By clicking here you will be")}",
            "&7${format("able to create a crate type.")}",
            "",
            "&6${format("Left-click:")} &e${format("Create Crate.")}"
        )

    @field:SimpleKey(node = "menu.items.last-page")
    var lastPageItem: SimpleItemBuilder = SimpleItemBuilder(Material.SPECTRAL_ARROW)
        .withName("&c${format("Previous page")}")
        .withSlots(36)

    @field:SimpleKey(node = "menu.items.next-page")
    var nextPageItem: SimpleItemBuilder = SimpleItemBuilder(Material.SPECTRAL_ARROW)
        .withName("&a${format("Next page")}")
        .withSlots(44)

    @field:SimpleKey(node = "menu.items.back")
    var backItem: SimpleItemBuilder = SimpleItemBuilder(Material.FEATHER)
        .withName("&c${format("Go back")}")
        .withSlots(49)

    @field:SimpleKey(node = "menu.items.filler")
    var fillerItem: SimpleItemBuilder = SimpleItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
        .withName("&7")
        .withSlots(45, 46, 47, 48, 50, 51, 52, 53)
}

