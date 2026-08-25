package com.simplesurvival.crates.menu.crate.manager.previewreward

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.lib.configuration.key.SimpleKey
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import com.simplesurvival.lib.configuration.yaml.YamlBuilder
import com.simplesurvival.lib.util.caps.SmallCapsConverter.format
import org.bukkit.Material

object CratePreviewRewardMenuYaml : YamlBuilder<SimpleCrates>(
    SimpleCrates.plugin,
    "menus/crate/manager/preview-reward/preview_reward.yml"
)
{

    @field:SimpleKey(node = "menu.title")
    var title: String = "${format("Preview Reward ➡")} %id%"

    @field:SimpleKey(node = "menu.rows")
    var rows: Int = 4

    @field:SimpleKey(node = "menu.items.text")
    var textItem: SimpleItemBuilder = SimpleItemBuilder(Material.NAME_TAG)
        .withName("&a${format("Text")}")
        .withLore(
            "&7${format("Configure the text shown above preview rewards.")}",
            "",
            "&f${format("Current text:")} &7%text%",
            "&f${format("Current text height:")} &7%text_height%",
            "",
            "&e${format("Tip:")} &7${format("Use")} &a%reward% &7${format("to show reward name.")}",
            "",
            "&6${format("Left-click:")} &e${format("Change text.")}",
            "&6${format("Shift + Left-click:")} &e${format("Add 1 to text height.")}",
            "&6${format("Right-click:")} &e${format("Add 0.1 to text height.")}",
            "&6${format("Shift + Right-click:")} &e${format("Remove 0.1 from text height.")}"
        )
        .withSlots(12)

    @field:SimpleKey(node = "menu.items.height-item")
    var heightItem: SimpleItemBuilder = SimpleItemBuilder(Material.ITEM_FRAME)
        .withName("&a${format("Item Height")}")
        .withLore(
            "&7${format("Configure the item preview height.")}",
            "",
            "&f${format("Current item height:")} &7%item_height%",
            "",
            "&6${format("Left-click:")} &e${format("Add 0.1.")}",
            "&6${format("Shift + Left-click:")} &e${format("Add 1.")}",
            "&6${format("Right-click:")} &e${format("Remove 0.1.")}",
            "&6${format("Shift + Right-click:")} &e${format("Remove 1.")}"
        )
        .withSlots(14)

    @field:SimpleKey(node = "menu.items.back")
    var backItem: SimpleItemBuilder = SimpleItemBuilder(Material.FEATHER)
        .withName("&c${format("Go back")}")
        .withSlots(31)

    @field:SimpleKey(node = "menu.items.filler")
    var fillerItem: SimpleItemBuilder = SimpleItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
        .withName("&7")
        .withSlots(27, 28, 29, 30, 32, 33, 34, 35)
}
