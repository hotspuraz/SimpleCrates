package com.simplesurvival.crates.menu.crate.manager.link

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.lib.configuration.key.SimpleKey
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import com.simplesurvival.lib.configuration.yaml.YamlBuilder
import com.simplesurvival.lib.util.caps.SmallCapsConverter.format
import org.bukkit.Material

object CrateLinkMenuYaml : YamlBuilder<SimpleCrates>(
    SimpleCrates.plugin,
    "menus/crate/manager/link/crate_link.yml"
)
{

    @field:SimpleKey(node = "menu.title")
    var title = "${format("Crate ➡ Link ➡")} %id%"

    @field:SimpleKey(node = "menu.rows")
    var rows: Int = 6

    @field:SimpleKey(node = "menu.items-per-page")
    var itemsPerPage: Int = 20

    @field:SimpleKey(node = "menu.start-items-page-slot")
    var startItemsPageSlot: Int = 10

    @field:SimpleKey(node = "menu.items.key")
    var keyItem: SimpleItemBuilder = SimpleItemBuilder(Material.TRIPWIRE_HOOK)
        .withName("&f%name% &7(${format("Id:")} %id%)")
        .withLore(
            "&7${format("By clicking here you will be")}",
            "&7${format("able to configure this key.")}",
            "",
            "&f${format("Enabled:")} &7%is_enabled%",
            "",
            "&6${format("Left-click:")} &e${format("Unlink Key.")}",
            "&6${format("Right-click:")} &e${format("Receive Key.")}"
        )

    @field:SimpleKey(node = "menu.items.link")
    var linkItem: SimpleItemBuilder = SimpleItemBuilder(Material.LIME_STAINED_GLASS_PANE)
        .withName("&a${format("Link Key")}")
        .withLore(
            "&7${format("By clicking here, you will be")}",
            "&7${format("able to link a crate to this key.")}",
            "",
            "&6${format("Left-click:")} &e${format("Link Key.")}"
        )

    @field:SimpleKey(node = "menu.items.required-enabled")
    var requiredKeyEnabled: SimpleItemBuilder = SimpleItemBuilder(Material.LIME_DYE)
        .withName("&a${format("Key Required")}")
        .withLore(
            "&7${format("By clicking here you will be able")}",
            "&7${format("to change if this crate requires")}",
            "&7${format("a key to be opened.")}",
            "",
            "&f${format("Enabled:")} &a${format("Yes")}",
            "",
            "&6${format("Left-click:")} &e${format("Switch State.")}"
        )
        .withSlots(47)

    @field:SimpleKey(node = "menu.items.required-disabled")
    var requiredKeyDisabled: SimpleItemBuilder = SimpleItemBuilder(Material.RED_DYE)
        .withName("&a${format("Key Required")}")
        .withLore(
            "&7${format("By clicking here you will be able")}",
            "&7${format("to change if this crate requires")}",
            "&7${format("a key to be opened.")}",
            "",
            "&f${format("Enabled:")} &c${format("No")}",
            "",
            "&6${format("Left-click:")} &e${format("Switch State.")}"
        )
        .withSlots(47)

    @field:SimpleKey(node = "menu.items.last-page")
    var lastPageItem: SimpleItemBuilder = SimpleItemBuilder(Material.SPECTRAL_ARROW)
        .withName("&c${format("Previous page")}")
        .withSlots(45)

    @field:SimpleKey(node = "menu.items.next-page")
    var nextPageItem: SimpleItemBuilder = SimpleItemBuilder(Material.SPECTRAL_ARROW)
        .withName("&a${format("Next page")}")
        .withSlots(53)

    @field:SimpleKey(node = "menu.items.back")
    var backItem: SimpleItemBuilder = SimpleItemBuilder(Material.FEATHER)
        .withName("&c${format("Go back")}")
        .withSlots(49)

    @field:SimpleKey(node = "menu.items.filler")
    var fillerItem: SimpleItemBuilder = SimpleItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
        .withName("&7")
        .withSlots(45, 46, 48, 50, 51, 52, 53)
}

