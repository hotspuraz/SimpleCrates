package com.simplesurvival.crates.menu.crate.manager.rewards.edit.win.commands

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.lib.configuration.key.SimpleKey
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import com.simplesurvival.lib.configuration.yaml.YamlBuilder
import com.simplesurvival.lib.util.caps.SmallCapsConverter.format
import org.bukkit.Material

object CrateRewardWinCommandsMenuYaml : YamlBuilder<SimpleCrates>(
    SimpleCrates.plugin,
    "menus/crate/manager/rewards/win/win_commands.yml"
)
{

    @field:SimpleKey(node = "menu.title")
    var title = "${format("Reward ➡ Commands ➡")} %id%"

    @field:SimpleKey(node = "menu.rows")
    var rows: Int = 4

    @field:SimpleKey(node = "menu.items-per-page")
    var itemsPerPage: Int = 6

    @field:SimpleKey(node = "menu.start-items-page-slot")
    var startItemsPageSlot: Int = 10

    @field:SimpleKey(node = "menu.items.command")
    var commandItem: SimpleItemBuilder = SimpleItemBuilder(Material.NAME_TAG)
        .withName("&a${format("Command #")}%id%")
        .withLore(
            "&f${format("Command:")} &7%command%",
            "",
            "&6${format("Left-click:")} &e${format("Edit command.")}",
            "&6${format("Right-click:")} &e${format("Remove command.")}"
        )

    @field:SimpleKey(node = "menu.items.add-command")
    var addCommandItem: SimpleItemBuilder = SimpleItemBuilder(Material.LIME_STAINED_GLASS_PANE)
        .withName("&a${format("Add Command")}")
        .withLore(
            "&7${format("By clicking here you will be able to add")}",
            "&7${format("a command that will be executed when")}",
            "&7${format("a player wins this reward.")}",
            "",
            "&7${format("Commands that starts with")} &a* &7${format("are executed by")}",
            "&7${format("players. Otherwise, they are executed by console.")}",
            "",
            "&7${format("You can also use the placeholder")} &a${format("%player%")}",
            "&7${format("and placeholders from")} &a${format("PlaceholderAPI")}&7.",
            "",
            "&6${format("Left-click:")} &e${format("Add a command.")}"
        )

    @field:SimpleKey(node = "menu.items.back")
    var backItem: SimpleItemBuilder = SimpleItemBuilder(Material.FEATHER)
        .withName("&c${format("Go back")}")
        .withSlots(31)

    @field:SimpleKey(node = "menu.items.filler")
    var fillerItem: SimpleItemBuilder = SimpleItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
        .withName("&7")
        .withSlots(27, 28, 29, 30, 32, 33, 34, 35)

    @field:SimpleKey(node = "menu.items.last-page")
    var lastPageItem: SimpleItemBuilder = SimpleItemBuilder(Material.SPECTRAL_ARROW)
        .withName("&c${format("Previous page")}")
        .withSlots(18)

    @field:SimpleKey(node = "menu.items.next-page")
    var nextPageItem: SimpleItemBuilder = SimpleItemBuilder(Material.SPECTRAL_ARROW)
        .withName("&a${format("Next page")}")
        .withSlots(26)
}

