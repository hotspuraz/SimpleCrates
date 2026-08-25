package com.simplesurvival.crates.menu.crate.manager.link.keys

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.lib.configuration.key.SimpleKey
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import com.simplesurvival.lib.configuration.yaml.YamlBuilder
import com.simplesurvival.lib.util.caps.SmallCapsConverter.format
import org.bukkit.Material

object CrateLinkKeysMenuYaml : YamlBuilder<SimpleCrates>(
    SimpleCrates.plugin,
    "menus/crate/manager/link/crate_link_keys.yml"
)
{

    @field:SimpleKey(node = "menu.title")
    var title = "${format("Crate ➡ Link ➡")} %id%"

    @field:SimpleKey(node = "menu.rows")
    var rows: Int = 4

    @field:SimpleKey(node = "menu.items-per-page")
    var itemsPerPage: Int = 14

    @field:SimpleKey(node = "menu.start-items-page-slot")
    var startItemsPageSlot: Int = 1

    @field:SimpleKey(node = "menu.items.key")
    var keyItem: SimpleItemBuilder = SimpleItemBuilder(Material.TRIPWIRE_HOOK)
        .withName("&f%name% &7(${format("Id:")} %id%)")
        .withLore(
            "&7${format("By clicking here, you will link")}",
            "&7${format("this key to the crate.")}",
            "",
            "&6${format("Left-click:")} &e${format("Select Key.")}"
        )

    @field:SimpleKey(node = "menu.items.last-page")
    var lastPageItem: SimpleItemBuilder = SimpleItemBuilder(Material.SPECTRAL_ARROW)
        .withName("&c${format("Previous page")}")
        .withSlots(18)

    @field:SimpleKey(node = "menu.items.next-page")
    var nextPageItem: SimpleItemBuilder = SimpleItemBuilder(Material.SPECTRAL_ARROW)
        .withName("&a${format("Next page")}")
        .withSlots(26)

    @field:SimpleKey(node = "menu.items.back")
    var backItem: SimpleItemBuilder = SimpleItemBuilder(Material.FEATHER)
        .withName("&c${format("Go back")}")
        .withSlots(31)

    @field:SimpleKey(node = "menu.items.filler")
    var fillerItem: SimpleItemBuilder = SimpleItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
        .withName("&7")
        .withSlots(27, 28, 29, 30, 32, 33, 34, 35)
}

