package com.simplesurvival.crates.menu.crate.manager.rewards.edit.win.items

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.lib.configuration.key.SimpleKey
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import com.simplesurvival.lib.configuration.yaml.YamlBuilder
import com.simplesurvival.lib.util.caps.SmallCapsConverter.format
import org.bukkit.Material

object CrateRewardWinItemsMenuYaml : YamlBuilder<SimpleCrates>(
    SimpleCrates.plugin,
    "menus/crate/manager/rewards/win/win_items.yml"
)
{

    @field:SimpleKey(node = "menu.title")
    var title = "${format("Reward ➡ Items ➡")} %id%"

    @field:SimpleKey(node = "menu.rows")
    var rows: Int = 6

    @field:SimpleKey(node = "menu.items-per-page")
    var itemsPerPage: Int = 20

    @field:SimpleKey(node = "menu.start-items-page-slot")
    var startItemsPageSlot: Int = 10

    @field:SimpleKey(node = "menu.items.add-item")
    var addItem: SimpleItemBuilder = SimpleItemBuilder(Material.LIME_STAINED_GLASS_PANE)
        .withName("&a${format("Add Item")}")
        .withLore(
            "&7${format("Drag an item from your inventory")}",
            "&7${format("here to add it as an item for this reward.")}"
        )

    @field:SimpleKey(node = "menu.items.item")
    var itemDisplay: SimpleItemBuilder = SimpleItemBuilder(Material.BARRIER)
        .withLore(
            "",
            "&6${format("Left-click:")} &e${format("Receive item.")}",
            "&6${format("Right-click:")} &e${format("Remove item.")}"
        )

    @field:SimpleKey(node = "menu.items.add-filler")
    var addFiller: SimpleItemBuilder = SimpleItemBuilder(Material.LIME_STAINED_GLASS_PANE)
        .withName("&a${format("Place the item here")}")
        .withSlots(
            "10-16", "19-25", "28-34"
        )

    @field:SimpleKey(node = "menu.items.back")
    var backItem: SimpleItemBuilder = SimpleItemBuilder(Material.FEATHER)
        .withName("&c${format("Go back")}")
        .withSlots(49)

    @field:SimpleKey(node = "menu.items.filler")
    var fillerItem: SimpleItemBuilder = SimpleItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
        .withName("&7")
        .withSlots(45, 46, 47, 48, 50, 51, 52, 53)

    @field:SimpleKey(node = "menu.items.last-page")
    var lastPageItem: SimpleItemBuilder = SimpleItemBuilder(Material.SPECTRAL_ARROW)
        .withName("&c${format("Previous page")}")
        .withSlots(36)

    @field:SimpleKey(node = "menu.items.next-page")
    var nextPageItem: SimpleItemBuilder = SimpleItemBuilder(Material.SPECTRAL_ARROW)
        .withName("&a${format("Next page")}")
        .withSlots(44)
}

