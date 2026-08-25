package com.simplesurvival.crates.menu.crate.preview

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.lib.configuration.key.SimpleKey
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import com.simplesurvival.lib.configuration.yaml.YamlBuilder
import com.simplesurvival.lib.util.caps.SmallCapsConverter.format
import org.bukkit.Material

object CratePreviewMenuYaml : YamlBuilder<SimpleCrates>(
    SimpleCrates.plugin,
    "menus/crate/crate_preview.yml"
)
{

    @field:SimpleKey(node = "menu.title")
    var title = "%name% ${format("Rewards")}"

    @field:SimpleKey(node = "menu.rows")
    var rows: Int = 6

    @field:SimpleKey(node = "menu.items-per-page")
    var itemsPerPage: Int = 21

    @field:SimpleKey(node = "menu.start-items-page-slot")
    var startItemsPageSlot: Int = 10

    @field:SimpleKey(node = "menu.items.reward")
    var rewardItem: SimpleItemBuilder = SimpleItemBuilder(Material.BARRIER)
        .withName("%name%")
        .withLore(
            "",
            "&f${format("Win Chance:")} &7%chance%"
        )

    @field:SimpleKey(node = "menu.items.reward-invalid")
    var rewardInvalidItem: SimpleItemBuilder = SimpleItemBuilder(Material.BARRIER)
        .withName("%name%")
        .withLore(
            "&7${format("This reward is invalid, make sure")}",
            "&7${format("to check if display item or")}",
            "&7${format("content items are set.")}",
            "",
            "&f${format("Win Chance:")} &6%chance%",
            "",
            "&c${format("Invalid Reward:")}&r &c&n${format("Found empty display item!")}"
        )

    @field:SimpleKey(node = "menu.items.available-keys")
    var availableKeysItem: SimpleItemBuilder = SimpleItemBuilder(Material.NETHER_STAR)
        .withName("&a${format("Available Keys")} &ex%keys%")
        .withLore(
            "&7${format("All the available keys, that")}",
            "&7${format("you can use on this crate.")}",
            "",
            "&f${format("Keys:")}",
            "%available_keys%"
        )
        .withSlots(51)

    @field:SimpleKey(node = "menu.items.last-page")
    var lastPageItem: SimpleItemBuilder = SimpleItemBuilder(Material.SPECTRAL_ARROW)
        .withName("&c${format("Previous page")}")
        .withSlots(36)

    @field:SimpleKey(node = "menu.items.next-page")
    var nextPageItem: SimpleItemBuilder = SimpleItemBuilder(Material.SPECTRAL_ARROW)
        .withName("&a${format("Next page")}")
        .withSlots(44)

    @field:SimpleKey(node = "menu.items.close")
    var closeItem: SimpleItemBuilder = SimpleItemBuilder(Material.BARRIER)
        .withName("&c${format("Close Menu")}")
        .withSlots(49)
        .withClickAction(
            listOf(
                "[CLOSE]"
            )
        )

    @field:SimpleKey(node = "menu.items.filler")
    var fillerItem: SimpleItemBuilder = SimpleItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
        .withName("&7")
        .withSlots(45, 46, 47, 48, 50, 52, 53)
}
