package com.simplesurvival.crates.menu.keys.manager

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.lib.configuration.key.SimpleKey
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import com.simplesurvival.lib.configuration.yaml.YamlBuilder
import com.simplesurvival.lib.util.caps.SmallCapsConverter.format
import org.bukkit.Material

object KeyManagerMenuYaml : YamlBuilder<SimpleCrates>(
    SimpleCrates.plugin,
    "menus/key/manager/key_manager.yml"
)
{

    @field:SimpleKey(node = "menu.title")
    var title: String = "${format("Key ➡")} %id%"

    @field:SimpleKey(node = "menu.rows")
    var rows: Int = 4

    @field:SimpleKey(node = "menu.items.identifier")
    var identifier: SimpleItemBuilder = SimpleItemBuilder(Material.NAME_TAG)
        .withName("&a${format("Identifier")}")
        .withLore(
            "&7${format("By clicking here you will be able")}",
            "&7${format("to rename your key identifier.")}",
            "",
            "&e&n${format("This identifier needs to be unique.")}",
            "",
            "&f${format("Identifier:")} &7%id%",
            "",
            "&6${format("Left-click:")} &e${format("Change Identifier.")}"
        )
        .withSlots(10)

    @field:SimpleKey(node = "menu.items.key-item")
    var keyItem: SimpleItemBuilder = SimpleItemBuilder(Material.TRIPWIRE_HOOK)
        .withName("&a${format("Item")}")
        .withLore(
            "&7${format("Display item that will")}",
            "&7${format("be used to represent this key.")}",
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

    @field:SimpleKey(node = "menu.items.link-crates")
    var linkCrates: SimpleItemBuilder = SimpleItemBuilder(Material.CHEST)
        .withName("&a${format("Link Crates")}")
        .withLore(
            "&7${format("By clicking here you will be able")}",
            "&7${format("to link crates with this key.")}",
            "",
            "&f${format("Crates linked:")} &7%linked_crates%",
            "",
            "&6${format("Left-click:")} &e${format("Navigate.")}"
        )
        .withSlots(13)

    @field:SimpleKey(node = "menu.items.glowing")
    var glowingItem: SimpleItemBuilder = SimpleItemBuilder(Material.ENCHANTED_BOOK)
        .withName("&a${format("Glowing Key")}")
        .withLore(
            "&7${format("By clicking here you will be able to")}",
            "&7${format("add glowing effect on the key.")}",
            "",
            "&f${format("Enabled:")} %is_enabled%",
            "",
            "&6${format("Left-click:")} &e${format("Switch State.")}"
        )
        .withSlots(15)

    @field:SimpleKey(node = "menu.items.virtual")
    var virtual: SimpleItemBuilder = SimpleItemBuilder(Material.EXPERIENCE_BOTTLE)
        .withName("&a${format("Virtual Key")}")
        .withLore(
            "&7${format("By clicking here you will be able to")}",
            "&7${format("set this key as virtual.")}",
            "",
            "&c&n${format("This means players will not")}",
            "&c&n${format("need physical keys.")}&r",
            "",
            "&f${format("Enabled:")} %is_enabled%",
            "",
            "&6${format("Left-click:")} &e${format("Switch State.")}"
        )
        .withSlots(16)

    @field:SimpleKey(node = "menu.items.enabled")
    var enabledItem: SimpleItemBuilder = SimpleItemBuilder(Material.LIME_DYE)
        .withName("&a${format("Enabled")}")
        .withLore(
            "&7${format("By clicking here you will be able")}",
            "&7${format("to change if this key is available.")}",
            "",
            "&f${format("Enabled:")} &aYes",
            "",
            "&6${format("Left-click:")} &e${format("Switch state.")}"
        )
        .withSlots(29)

    @field:SimpleKey(node = "menu.items.disabled")
    var disabledItem: SimpleItemBuilder = SimpleItemBuilder(Material.RED_DYE)
        .withName("&c${format("Disabled")}")
        .withLore(
            "&7${format("By clicking here you will be able")}",
            "&7${format("to change if this key is available.")}",
            "",
            "&f${format("Enabled:")} &cNo",
            "",
            "&6${format("Left-click:")} &e${format("Switch state.")}"
        )
        .withSlots(29)

    @field:SimpleKey(node = "menu.items.back")
    var backItem: SimpleItemBuilder = SimpleItemBuilder(Material.FEATHER)
        .withName("&c${format("Go back")}")
        .withSlots(31)

    @field:SimpleKey(node = "menu.items.clone-key")
    var cloneKey: SimpleItemBuilder = SimpleItemBuilder(Material.OAK_SIGN)
        .withName("&a${format("Clone key")}")
        .withLore(
            "&7${format("By clicking here you will be")}",
            "&7${format("able to clone this key.")}",
            "",
            "&6${format("Left-click:")} &e${format("Duplicate key.")}"
        )
        .withSlots(33)

    @field:SimpleKey(node = "menu.items.delete-key")
    var deleteKey: SimpleItemBuilder = SimpleItemBuilder(Material.BARRIER)
        .withName("&c${format("Delete Key")}")
        .withLore(
            "&7${format("By clicking here this key")}",
            "&7${format("will be deleted from the server.")}",
            "",
            "&e&n${format("All keys in the world will no longer be valid.")}",
            "",
            "&6${format("Left-click:")} &e${format("Delete key.")}"
        )
        .withSlots(34)

    @field:SimpleKey(node = "menu.items.filler")
    var filler: SimpleItemBuilder = SimpleItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
        .withName("&7")
        .withSlots(27, 28, 30, 32, 35)
}

