package com.simplesurvival.crates.menu.crate.manager.rewards

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.lib.configuration.key.SimpleKey
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import com.simplesurvival.lib.configuration.yaml.YamlBuilder
import com.simplesurvival.lib.util.caps.SmallCapsConverter.format
import org.bukkit.Material

object CrateRewardsMenuYaml : YamlBuilder<SimpleCrates>(
    SimpleCrates.plugin,
    "menus/crate/manager/rewards/crate_rewards.yml"
)
{

    @field:SimpleKey(node = "menu.title")
    var title = "${format("Crate ➡ Rewards ➡")} %id%"

    @field:SimpleKey(node = "menu.rows")
    var rows: Int = 6

    @field:SimpleKey(node = "menu.items-per-page")
    var itemsPerPage: Int = 21

    @field:SimpleKey(node = "menu.start-items-page-slot")
    var startItemsPageSlot: Int = 10

    @field:SimpleKey(node = "menu.current-max-win-rewards")
    var currentMaxWinRewards: Int = 1

    @field:SimpleKey(node = "menu.max-win-rewards")
    var maxWinRewards: Int = 30

    @field:SimpleKey(node = "menu.items.reorder")
    var reorderItem: SimpleItemBuilder = SimpleItemBuilder(Material.REPEATER)
        .withName("&a${format("Reordering")}")
        .withLore(
            "&7${format("By clicking here you can sort")}",
            "&7${format("the rewards in the order you want.")}",
            "",
            "&f${format("Enabled:")} %is_enabled%",
            "",
            "&6${format("Left-click:")} &e${format("Switch State.")}"
        )
        .withSlots(47)

    @field:SimpleKey(node = "menu.items.max-win")
    var maxWinItem: SimpleItemBuilder = SimpleItemBuilder(Material.AMETHYST_CLUSTER)
        .withName("&a${format("Max Win Rewards")}")
        .withLore(
            "&7${format("By clicking here you can define")}",
            "&7${format("how many rewards a player will win at once.")}",
            "",
            "&f${format("Amount:")} &7%max_win_rewards%",
            "",
            "&6${format("Left-click:")} &e${format("Add 1.")}",
            "&6${format("Right-click:")} &e${format("Remove 1.")}",
            "&6${format("Shift + Left-click:")} &e${format("Add 10.")}",
            "&6${format("Shift + Right-click:")} &e${format("Remove 10.")}"
        )
        .withSlots(51)

    @field:SimpleKey(node = "menu.items.add-reward")
    var addRewardItem: SimpleItemBuilder = SimpleItemBuilder(Material.LIME_STAINED_GLASS_PANE)
        .withName("&a${format("Add Reward")}")
        .withLore(
            "&7${format("By clicking here you can start")}",
            "&7${format("setting up a new reward in this crate.")}",
            "",
            "&6${format("Left-click:")} &e${format("Add Reward.")}",
            "&6${format("Shift + Left-click:")} &e${format("Add Multiple Rewards.")}"
        )

    @field:SimpleKey(node = "menu.items.reward")
    var rewardItem: SimpleItemBuilder = SimpleItemBuilder(Material.BARRIER)
        .withName("&a${format("Reward #")}%id%")
        .withLore(
            "",
            "&f${format("Weight:")} &7%weight%",
            "&f${format("Chance:")} &7%chance%",
            "",
            "&6${format("Left-click:")} &e${format("Edit Reward.")}",
            "&6${format("Right-click:")} &e${format("Remove Reward.")}"
        )

    @field:SimpleKey(node = "menu.items.reward-invalid")
    var rewardInvalidItem: SimpleItemBuilder = SimpleItemBuilder(Material.BARRIER)
        .withName("&a${format("Reward #")}%id%")
        .withLore(
            "&7${format("This reward is invalid, make sure")}",
            "&7${format("to check if display item or")}",
            "&7${format("content items are set.")}",
            "",
            "&f${format("Weight:")} &7%weight%",
            "&f${format("Chance:")} &7%chance%",
            "",
            "&c${format("Invalid Reward:")}&r &c&n${format("Found empty display item!")}",
            "",
            "&6${format("Left-click:")} &e${format("Edit Reward.")}",
            "&6${format("Right-click:")} &e${format("Remove Reward.")}"
        )

    @field:SimpleKey(node = "menu.items.reward-reorder-lore")
    var rewardReorderLore: List<String> = listOf(
        "&7${format("Move that reward and")}",
        "&7${format("place it wherever you like.")}",
        "",
        "&6${format("Left-click:")} &e${format("Move Reward.")}",
    )

    @field:SimpleKey(node = "menu.items.last-page")
    var lastPageItem: SimpleItemBuilder = SimpleItemBuilder(Material.SPECTRAL_ARROW)
        .withName("&c${format("Previous page")}")
        .withSlots(36)

    @field:SimpleKey(node = "menu.items.next-page")
    var nextPageItem: SimpleItemBuilder = SimpleItemBuilder(Material.SPECTRAL_ARROW)
        .withName("&a${format("Next page")}")
        .withSlots(44)

    @field:SimpleKey(node = "menu.items.back")
    var backItem: SimpleItemBuilder = SimpleItemBuilder(Material.FEATHER)
        .withName("&c${format("Go back")}")
        .withSlots(49)

    @field:SimpleKey(node = "menu.items.filler")
    var fillerItem: SimpleItemBuilder = SimpleItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
        .withName("&7")
        .withSlots(45, 46, 48, 50, 52, 53)
}

