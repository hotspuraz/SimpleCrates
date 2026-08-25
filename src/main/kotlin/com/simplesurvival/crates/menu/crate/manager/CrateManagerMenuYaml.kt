package com.simplesurvival.crates.menu.crate.manager

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.lib.configuration.key.SimpleKey
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import com.simplesurvival.lib.configuration.yaml.YamlBuilder
import com.simplesurvival.lib.util.caps.SmallCapsConverter.format
import org.bukkit.Material

object CrateManagerMenuYaml : YamlBuilder<SimpleCrates>(
    SimpleCrates.plugin,
    "menus/crate/manager/crate.yml"
)
{

    @field:SimpleKey(node = "menu.title")
    var title: String = "${format("Crate ➡")} %id%"

    @field:SimpleKey(node = "menu.rows")
    var rows: Int = 5

    @field:SimpleKey(node = "menu.items.identifier")
    var identifier: SimpleItemBuilder = SimpleItemBuilder(Material.NAME_TAG)
        .withName("&a${format("Identifier")}")
        .withLore(
            "&7${format("By clicking here you will be able")}",
            "&7${format("to rename your crate identifier.")}",
            "",
            "&e&n${format("This identifier needs to be unique.")}",
            "",
            "&f${format("Identifier:")} &7%id%",
            "",
            "&6${format("Left-click:")} &e${format("Change Identifier.")}"
        )
        .withSlots(10)

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
            "&6${format("Left-click:")} &e${format("Edit Item.")}",
            "&6${format("Drag & Drop:")} &e${format("Replace Item.")}"
        )
        .withSlots(11)

    @field:SimpleKey(node = "menu.items.keys")
    var keys: SimpleItemBuilder = SimpleItemBuilder(Material.TRIPWIRE_HOOK)
        .withName("&a${format("Link keys")}")
        .withLore(
            "&7${format("By clicking here you will be able")}",
            "&7${format("to link keys with this crate.")}",
            "",
            "&f${format("Keys linked:")} &7%linked_keys%",
            "&f${format("Required key:")} %required_key%",
            "",
            "&6${format("Left-click:")} &e${format("Navigate.")}"
        )
        .withSlots(15)

    @field:SimpleKey(node = "menu.items.rewards")
    var rewards: SimpleItemBuilder = SimpleItemBuilder(Material.EMERALD)
        .withName("&a${format("Rewards")}")
        .withLore(
            "&7${format("By clicking here you will be able to")}",
            "&7${format("configure all the rewards that a player")}",
            "&7${format("can earn by opening this crate.")}",
            "",
            "&6${format("Left-click:")} &e${format("Navigate.")}"
        )
        .withSlots(13)

    @field:SimpleKey(node = "menu.items.hologram")
    var hologram: SimpleItemBuilder = SimpleItemBuilder(Material.ARMOR_STAND)
        .withName("&a${format("Hologram")}")
        .withLore(
            "&7${format("By clicking here you will be able to")}",
            "&7${format("configure the hologram above this crate.")}",
            "",
            "&6${format("Left-click:")} &e${format("Navigate.")}"
        )
        .withSlots(16)

    @field:SimpleKey(node = "menu.items.engine")
    var engine: SimpleItemBuilder = SimpleItemBuilder(Material.TARGET)
        .withName("&a${format("Engine Mode")}")
        .withLore(
            "&7${format("By clicking here you will be able")}",
            "&7${format("change engine that this crate will use.")}",
            "",
            "&b${format("Vanilla Block Engine:")}",
            "&7${format("Default engine of a crate, the representation will")}",
            "&7${format("be the material chosen as the block of this crate.")}",
            "",
            "&b${format("Vanilla Model Engine:")} &9${format("(1.12+)")}",
            "&7${format("Possibility to assign a custom static model to the crate")}",
            "&7${format("but does not support animations. This supports vanilla models")}",
            "&7${format("through material and custom model data.")}",
            "",
            "&b⚙ &f${format("Engines:")}",
            "%engines%",
            "",
            "&6${format("Left-click:")} &e${format("Swap Engine.")}",
            "&6${format("Right-click:")} &e${format("Edit Engine.")}"
        )
        .withSlots(19)

    @field:SimpleKey(node = "menu.items.broadcast-message")
    var broadcastMessage: SimpleItemBuilder = SimpleItemBuilder(Material.ITEM_FRAME)
        .withName("&a${format("Broadcast Message")}")
        .withLore(
            "&7${format("By clicking here you will be able")}",
            "&7${format("to configure the broadcast message")}",
            "&7${format("for the rewards.")}",
            "",
            "&e${format("Placeholders available:")}",
            "&7${format("%player%, %reward%, %crate%")}",
            "",
            "&f${format("Broadcast Message")}",
            "%broadcast_message%",
            "",
            "&6${format("Left-click:")} &e${format("Add Broadcast Message.")}",
            "&6${format("Right-click:")} &e${format("Remove Last One.")}",
            "&6${format("Shift + Right-click:")} &e${format("Clear All.")}"
        )
        .withSlots(20)

    @field:SimpleKey(node = "menu.items.preview-reward-enabled")
    var previewRewardEnabled: SimpleItemBuilder = SimpleItemBuilder(Material.GOLDEN_APPLE)
        .withName("&a${format("Preview Reward")}")
        .withLore(
            "&7${format("By clicking here you will enable")}",
            "&7${format("a preview reward above each crate.")}",
            "",
            "&f${format("Enabled:")} &a${format("Yes")}",
            "",
            "&6${format("Left-click:")} &e${format("Switch State.")}",
            "&6${format("Right-click:")} &e${format("Edit Preview Reward.")}"
        )
        .withSlots(22)

    @field:SimpleKey(node = "menu.items.preview-reward-disabled")
    var previewRewardDisabled: SimpleItemBuilder = SimpleItemBuilder(Material.GOLDEN_APPLE)
        .withName("&c${format("Preview Reward")}")
        .withLore(
            "&7${format("By clicking here you will enable")}",
            "&7${format("a preview reward above each crate.")}",
            "",
            "&f${format("Enabled:")} &c${format("No")}",
            "",
            "&6${format("Left-click:")} &e${format("Switch State.")}",
            "&6${format("Right-click:")} &e${format("Edit Preview Reward.")}"
        )
        .withSlots(22)

    @field:SimpleKey(node = "menu.items.animation-enabled")
    var animationEnabled: SimpleItemBuilder = SimpleItemBuilder(Material.NETHER_STAR)
        .withName("&a${format("Open Animation")}")
        .withLore(
            "&7${format("By clicking here you will be able")}",
            "&7${format("to switch the opening animation")}",
            "&7${format("for this crate.")}",
            "",
            "&f${format("Enabled:")} %is_enabled%",
            "",
            "&6${format("Left-click:")} &e${format("Switch State.")}"
        )
        .withSlots(25)

    @field:SimpleKey(node = "menu.items.permission-requirement")
    var permissionRequirement: SimpleItemBuilder = SimpleItemBuilder(Material.BAMBOO_FENCE)
        .withName("&a${format("Permission Requirement")}")
        .withLore(
            "&7${format("By clicking here you will set")}",
            "&7${format("the permission a player must have")}",
            "&7${format("to open this crate.")}",
            "",
            "&f${format("Required:")} %is_required%",
            "&f${format("Permission:")} &7%permission%",
            "",
            "&6${format("Left-click:")} &e${format("Switch State.")}",
            "&6${format("Right-click:")} &e${format("Change Permission.")}"
        )
        .withSlots(24)

    @field:SimpleKey(node = "menu.items.enabled")
    var enabledItem: SimpleItemBuilder = SimpleItemBuilder(Material.LIME_DYE)
        .withName("&a${format("Enabled")}")
        .withLore(
            "&7${format("By clicking here you will be able")}",
            "&7${format("to change if this crate is available.")}",
            "",
            "&f${format("Enabled:")} &aYes",
            "",
            "&6${format("Left-click:")} &e${format("Switch state.")}"
        )
        .withSlots(37)

    @field:SimpleKey(node = "menu.items.disabled")
    var disabledItem: SimpleItemBuilder = SimpleItemBuilder(Material.RED_DYE)
        .withName("&c${format("Disabled")}")
        .withLore(
            "&7${format("By clicking here you will be able")}",
            "&7${format("to change if this crate is available.")}",
            "",
            "&f${format("Enabled:")} &cNo",
            "",
            "&6${format("Left-click:")} &e${format("Switch state.")}"
        )
        .withSlots(37)

    @field:SimpleKey(node = "menu.items.attack-block")
    var attackBlock: SimpleItemBuilder = SimpleItemBuilder(Material.HONEY_BLOCK)
        .withName("&a${format("Attach block")}")
        .withLore(
            "&7${format("By clicking here you will be able")}",
            "&7${format("to create a crate by attaching a block.")}",
            "",
            "&6${format("Left-click:")} &e${format("Set location.")}"
        )
        .withSlots(38)

    @field:SimpleKey(node = "menu.items.back")
    var backItem: SimpleItemBuilder = SimpleItemBuilder(Material.FEATHER)
        .withName("&c${format("Go back")}")
        .withSlots(40)

    @field:SimpleKey(node = "menu.items.clone-crate")
    var cloneCrate: SimpleItemBuilder = SimpleItemBuilder(Material.OAK_SIGN)
        .withName("&a${format("Clone crate")}")
        .withLore(
            "&7${format("By clicking here you will be")}",
            "&7${format("able to clone this crate.")}",
            "",
            "&6${format("Left-click:")} &e${format("Duplicate crate.")}"
        )
        .withSlots(42)

    @field:SimpleKey(node = "menu.items.delete-crate")
    var deleteCrate: SimpleItemBuilder = SimpleItemBuilder(Material.BARRIER)
        .withName("&c${format("Delete crate")}")
        .withLore(
            "&7${format("By clicking here this crate")}",
            "&7${format("will be deleted from the server.")}",
            "",
            "&e&n${format("All keys in this crate will no longer be valid.")}",
            "",
            "&6${format("Left-click:")} &e${format("Delete crate.")}"
        )
        .withSlots(43)

    @field:SimpleKey(node = "menu.items.filler")
    var filler: SimpleItemBuilder = SimpleItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
        .withName("&7")
        .withSlots(36, 39, 41, 44)
}

