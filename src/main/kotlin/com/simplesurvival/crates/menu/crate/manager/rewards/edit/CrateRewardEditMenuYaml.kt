package com.simplesurvival.crates.menu.crate.manager.rewards.edit

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.lib.configuration.key.SimpleKey
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import com.simplesurvival.lib.configuration.yaml.YamlBuilder
import com.simplesurvival.lib.util.caps.SmallCapsConverter.format
import org.bukkit.Material

object CrateRewardEditMenuYaml : YamlBuilder<SimpleCrates>(
    SimpleCrates.plugin,
    "menus/crate/manager/rewards/reward_edit.yml"
)
{

    @field:SimpleKey(node = "menu.title")
    var title = "${format("Reward ➡ Edit ➡")} %id%"

    @field:SimpleKey(node = "menu.rows")
    var rows: Int = 6

    @field:SimpleKey(node = "menu.infinite-symbol")
    var infiniteSymbol: String = "∞"

    @field:SimpleKey(node = "menu.items.display")
    var displayItem = SimpleItemBuilder(Material.IRON_BARS)
        .withName("&a${format("Display Item")}")
        .withLore(
            "&7${format("This item will serve as the display")}",
            "&7${format("representing the reward for this crate.")}",
            "",
            "&f${format("Item:")}",
            " &f${format("Name:")} &7%name%",
            " &f${format("Lore:")}",
            "%lore%",
            "",
            "&6${format("Left-click:")} &e${format("Edit Item.")}",
            "&6${format("Right-click:")} &e${format("Receive Item.")}",
            "&6${format("Drag & Drop:")} &e${format("Replace Item.")}"
        )
        .withSlots(4)

    @field:SimpleKey(node = "menu.items.display-empty")
    var displayEmptyItem = SimpleItemBuilder(Material.IRON_BARS)
        .withName("&a${format("Display Item")}")
        .withLore(
            "&7${format("This item will serve as the display")}",
            "&7${format("representing the reward for this crate.")}",
            "",
            "&f${format("Item:")} &7${format("Empty")}",
            "",
            "&6${format("Left-click:")} &e${format("Edit Item.")}",
            "&6${format("Right-click:")} &e${format("Receive Item.")}",
            "&6${format("Drag & Drop:")} &e${format("Replace Item.")}"
        )
        .withSlots(4)

    @field:SimpleKey(node = "menu.items.win-items")
    var winItems = SimpleItemBuilder(Material.GREEN_SHULKER_BOX)
        .withName("&a${format("Win Items")}")
        .withLore(
            "&7${format("By clicking here you will be able")}",
            "&7${format("to add items that the player will")}",
            "&7${format("receive when winning this reward.")}",
            "",
            "&6${format("Left-click:")} &e${format("Navigate.")}"
        )
        .withSlots(19)

    @field:SimpleKey(node = "menu.items.win-commands")
    var winCommandsItem = SimpleItemBuilder(Material.COMMAND_BLOCK)
        .withName("&a${format("Win Commands")}")
        .withLore(
            "&7${format("By clicking here you can manage")}",
            "&7${format("all the commands that will be executed")}",
            "&7${format("when a player wins this reward.")}",
            "",
            "&6${format("Left-click:")} &e${format("Navigate.")}"
        )
        .withSlots(21)


    @field:SimpleKey(node = "menu.items.win-limit")
    var winLimitItem = SimpleItemBuilder(Material.GUNPOWDER)
        .withName("&a${format("Win Limit")}")
        .withLore(
            "&7${format("This option sets how many times")}",
            "&7${format("a player can win this reward.")}",
            "",
            "&f${format("Win Limit:")} &7%win_limit%",
            "",
            "&6${format("Left-click:")} &e${format("Add 1.")}",
            "&6${format("Right-click:")} &e${format("Remove 1.")}",
            "&6${format("Shift + Left-click:")} &e${format("Add 10.")}",
            "&6${format("Shift + Right-click:")} &e${format("Remove 10.")}"
        )
        .withSlots(23)

    @field:SimpleKey(node = "menu.items.weight")
    var weightItem = SimpleItemBuilder(Material.HOPPER)
        .withName("&a${format("Weight")} &8${format("(Random Mode)")}")
        .withLore(
            "&7${format("The weight will contribute to a")}",
            "&7${format("player's chance of winning this item.")}",
            "",
            "&f${format("Weight:")} &7%weight%",
            "",
            "&6${format("Left-click:")} &e${format("Add 1.")}",
            "&6${format("Right-click:")} &e${format("Remove 1.")}",
            "&6${format("Shift + Left-click:")} &e${format("Add 10.")}",
            "&6${format("Shift + Right-click:")} &e${format("Remove 10.")}"
        )
        .withSlots(25)

    @field:SimpleKey(node = "menu.items.broadcast")
    var broadcastItem = SimpleItemBuilder(Material.GOAT_HORN)
        .withName("&a${format("Broadcast Message")}")
        .withLore(
            "&7${format("This option set if a broadcast message")}",
            "&7${format("should appear when a player wins this reward.")}",
            "",
            "&f${format("Enabled:")} %is_enabled%",
            "",
            "&6${format("Left-click:")} &e${format("Switch State.")}",
        )
        .hideFlags()
        .withSlots(29)

    @field:SimpleKey(node = "menu.items.restricted-permissions")
    var restrictedPermissionsItem = SimpleItemBuilder(Material.GLOBE_BANNER_PATTERN)
        .withName("&a${format("Restricted Permissions")}")
        .withLore(
            "&7${format("This option defines the")}",
            "&7${format("restricted permissions the player")}",
            "&7${format("cannot have to win this reward.")}",
            "",
            "&c&n${format("This will not apply if the player has op.")}",
            "",
            "&f${format("Restricted Permissions:")}",
            "%restricted_permissions%",
            "",
            "&6${format("Left-click:")} &e${format("Add Permission.")}",
            "&6${format("Right-click:")} &e${format("Remove Last One.")}",
            "&6${format("Shift + Right-click:")} &e${format("Clear All.")}"
        )
        .withSlots(33)

    @field:SimpleKey(node = "menu.items.identifier")
    var identifierItem = SimpleItemBuilder(Material.NAME_TAG)
        .withName("&a${format("Change Identifier")}")
        .withLore(
            "&7${format("By clicking here you will be able")}",
            "&7${format("to rename your reward identifier.")}",
            "",
            "&c&n${format("This identifier need to be unique.")}",
            "",
            "&f${format("Current Identifier:")} &7%id%",
            "",
            "&6${format("Left-click:")} &e${format("Change Identifier.")}",
        )
        .withSlots(47)

    @field:SimpleKey(node = "menu.items.delete")
    var deleteItem = SimpleItemBuilder(Material.BARRIER)
        .withName("&c${format("Delete Reward")}")
        .withLore(
            "&7${format("By clicking here this reward")}",
            "&7${format("will be deleted from this crate.")}",
            "",
            "&6${format("Left-click:")} &e${format("Delete Reward.")}",
        )
        .withSlots(51)

    @field:SimpleKey(node = "menu.items.back")
    var backItem = SimpleItemBuilder(Material.FEATHER)
        .withName("&c${format("Go back")}")
        .withSlots(49)

    @field:SimpleKey(node = "menu.items.filler")
    var fillerItem = SimpleItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
        .withName("&7")
        .withSlots(45, 46, 48, 50, 52, 53)
}

