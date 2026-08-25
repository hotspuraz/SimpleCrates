package com.simplesurvival.crates.menu.keys

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.lib.configuration.key.SimpleKey
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import com.simplesurvival.lib.configuration.yaml.YamlBuilder
import com.simplesurvival.lib.util.caps.SmallCapsConverter.format
import org.bukkit.Material

object KeysMenuYaml : YamlBuilder<SimpleCrates>(
    SimpleCrates.plugin,
    "menus/key/keys.yml"
)
{

    @field:SimpleKey(node = "menu.title")
    var title: String = format("Editor ➡ Keys")

    @field:SimpleKey(node = "menu.rows")
    var rows: Int = 6

    @field:SimpleKey(node = "menu.items-per-page")
    var itemsPerPage: Int = 20

    @field:SimpleKey(node = "menu.start-items-page-slot")
    var startItemsPageSlot: Int = 10

    @field:SimpleKey(node = "menu.items.key")
    var keyItem: SimpleItemBuilder = SimpleItemBuilder(Material.TRIPWIRE_HOOK)
        .withName(
            "%name% &7(${format("Id:")} %id%)"
        )
        .withLore(
            "&7${format("By clicking here you will be")}",
            "&7${format("able to configure this key.")}",
            "",
            "&f${format("Enabled:")} %is_enabled%",
            "&f${format("Crates Linked:")} %crates_linked%",
            "",
            "&6${format("Left-click:")} &e${format("Navigate.")}",
            "&6${format("Right-click:")} &e${format("Receive Key.")}",
        )

    @field:SimpleKey(node = "menu.items.add-key")
    var addKeyItem: SimpleItemBuilder = SimpleItemBuilder(Material.LIME_STAINED_GLASS_PANE)
        .withName("&a${format("Add Key")}")
        .withLore(
            "&7${format("By clicking here you will be")}",
            "&7${format("able to create a key.")}",
            "",
            "&6${format("Left-click:")} &e${format("Create Key.")}"
        )

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
        .withSlots(45, 46, 47, 48, 50, 51, 52, 53)
}

