package com.simplesurvival.crates.menu.crate.manager.rewards.edit.delete

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.lib.configuration.key.SimpleKey
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import com.simplesurvival.lib.configuration.yaml.YamlBuilder
import com.simplesurvival.lib.util.caps.SmallCapsConverter.format
import org.bukkit.Material

object CrateRewardDeleteMenuYaml : YamlBuilder<SimpleCrates>(
    SimpleCrates.plugin,
    "menus/crate/manager/rewards/delete.yml"
)
{

    @field:SimpleKey(node = "menu.title")
    var title = "${format("Reward ➡ Delete")} %id%"

    @field:SimpleKey(node = "menu.rows")
    var rows: Int = 3

    @field:SimpleKey(node = "menu.items.display")
    var display: SimpleItemBuilder = SimpleItemBuilder(Material.BARRIER)
        .withName("&a${format("Display Item")}")
        .withLore(
            "&7${format("This item will serve as the display")}",
            "&7${format("representing the reward for this crate.")}"
        )
        .withSlots(13)

    @field:SimpleKey(node = "menu.items.cancel")
    var cancel: SimpleItemBuilder = SimpleItemBuilder(Material.RED_STAINED_GLASS_PANE)
        .withName("&c${format("Cancel")}")
        .withSlots(0, 1, 2, 9, 10, 11, 18, 19, 20)

    @field:SimpleKey(node = "menu.items.confirm")
    var confirm: SimpleItemBuilder = SimpleItemBuilder(Material.LIME_STAINED_GLASS_PANE)
        .withName("&a${format("Confirm")}")
        .withSlots(6, 7, 8, 15, 16, 17, 24, 25, 26)

}

