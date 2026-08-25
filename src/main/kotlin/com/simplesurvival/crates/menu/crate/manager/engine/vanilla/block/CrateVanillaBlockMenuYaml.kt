package com.simplesurvival.crates.menu.crate.manager.engine.vanilla.block

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.lib.configuration.key.SimpleKey
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import com.simplesurvival.lib.configuration.yaml.YamlBuilder
import com.simplesurvival.lib.util.caps.SmallCapsConverter.format
import org.bukkit.Material

object CrateVanillaBlockMenuYaml : YamlBuilder<SimpleCrates>(
    SimpleCrates.plugin,
    "menus/crate/manager/engine/vanilla/block/vanilla_block.yml"
)
{

    @field:SimpleKey(node = "menu.title")
    var title: String = "${format("Vanilla Block ➡")} %id%"

    @field:SimpleKey(node = "menu.rows")
    var rows: Int = 4

    @field:SimpleKey(node = "menu.items.block-material")
    var blockMaterial: SimpleItemBuilder = SimpleItemBuilder(Material.CHEST)
        .withName("&a${format("Block Material")}")
        .withLore(
            "&7${format("By clicking here you will be")}",
            "&7${format("able to change the material that")}",
            "&7${format("is used for the crate block.")}",
            "",
            "&f${format("Material:")} &7%material%",
            "",
            "&6${format("Left-click:")} &e${format("Choose Material.")}",
            "&6${format("Right-click:")} &e${format("Update Block.")}"
        )
        .withSlots(13)

    @field:SimpleKey(node = "menu.items.back")
    var backItem: SimpleItemBuilder = SimpleItemBuilder(Material.FEATHER)
        .withName("&c${format("Go back")}")
        .withSlots(31)

    @field:SimpleKey(node = "menu.items.filler")
    var filler: SimpleItemBuilder = SimpleItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
        .withName("&7")
        .withSlots(27, 28, 29, 30, 32, 33, 34, 35)
}

