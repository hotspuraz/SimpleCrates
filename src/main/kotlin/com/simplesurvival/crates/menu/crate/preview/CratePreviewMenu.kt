package com.simplesurvival.crates.menu.crate.preview

import com.simplesurvival.crates.menu.crate.CratesMenuYaml
import com.simplesurvival.crates.menu.crate.manager.rewards.RewardPageEntry
import com.simplesurvival.crates.menu.crate.manager.rewards.RewardPageView
import com.simplesurvival.crates.menu.crate.manager.rewards.RewardPagination
import com.simplesurvival.crates.service.crate.Crate
import com.simplesurvival.crates.service.crate.objects.reward.Reward
import com.simplesurvival.crates.service.profile.CrateProfileService
import com.simplesurvival.crates.util.DisplayNameFormatUtil
import com.simplesurvival.crates.util.ItemNaming
import com.simplesurvival.lib.menu.api.Menu
import com.simplesurvival.lib.menu.api.item.Item
import com.simplesurvival.lib.menu.api.sound.MenuSound
import com.simplesurvival.lib.util.kyori.TextUtil
import com.simplesurvival.lib.util.number.NumberUtil
import org.bukkit.Material
import org.bukkit.entity.Player

class CratePreviewMenu(
    player: Player,
    val crate: Crate
) : Menu(
    player,
    CratePreviewMenuYaml.title.replace("%name%", DisplayNameFormatUtil.normalize(crate.item.rawDisplayName.ifBlank { crate.identifier })),
    CratePreviewMenuYaml.rows,
    null,
    CratePreviewMenuYaml.itemsPerPage
)
{

    override fun build()
    {
        clear()

        // Rewards

        val rewards = crate.rewards
        val pageSlots = RewardPagination.rewardSlots(
            startSlot = CratePreviewMenuYaml.startItemsPageSlot,
            itemsPerPage = CratePreviewMenuYaml.itemsPerPage,
            itemPerRow = itemPerRow
        )
        val pageView = paginatePreviewRewards(rewards, pageSlots)
        if (pageNumber != pageView.page)
        {
            pageNumber = pageView.page
        }

        val totalWeight = rewards.sumOf { it.weight }

        pageView.entries.forEach { entry ->
            val reward = entry.reward
            val isInvalid = reward.item.isEmpty

            val rewardItem =
                if (!isInvalid) CratePreviewMenuYaml.rewardItem else CratePreviewMenuYaml.rewardInvalidItem

            val chance = if (totalWeight > 0.0)
                (reward.weight / totalWeight) * 100.0
            else 0.0

            val lore = mutableListOf<String>()

            lore.addAll(reward.item.rawLore)
            lore.addAll(rewardItem.rawLore)

            add(
                entry.slot,
                (if (!isInvalid) Item.fromStack(reward.item.cloneBuilder()) else Item.of(Material.BARRIER))
                    .name(rewardItem.rawDisplayName.replace("%name%", ItemNaming.displayNameOrMaterial(reward.item)))
                    .loreStrings(
                        TextUtil.replacedLoreString(
                            lore,
                            mapOf(
                                Pair("%chance%", "${NumberUtil.format(chance)}%")
                            )
                        )
                    )
            )
        }

        // Available Keys

        val availableKeyItem = CratePreviewMenuYaml.availableKeysItem

        val profile = CrateProfileService.instance.read(player.uniqueId)

        val crateKeys = crate.linkedKeys()
        val totalKeys = crateKeys.sumOf { key ->
            profile.keys()[key.identifier] ?: 0
        }

        val keyItemLore = mutableListOf<String>()

        availableKeyItem.rawLore.forEach { line ->

            if (line.contains("%available_keys%"))
            {

                if (crateKeys.isEmpty())
                    keyItemLore.add("&7(Empty)")
                else
                {
                    var id = 1

                    crateKeys.forEach { key ->

                        val amount = profile.keys()[key.identifier] ?: 0

                        keyItemLore.add(
                            "&7$id. ${key.item.rawDisplayName} &ex${NumberUtil.formatInt(amount)}"
                        )

                        id++
                    }
                }
            } else
                keyItemLore.add(line)
        }

        add(
            availableKeyItem.slot, Item.fromStack(availableKeyItem)
                .name(
                    availableKeyItem.rawDisplayName.replace("%keys%", NumberUtil.formatInt(totalKeys))
                )
                .loreStrings(keyItemLore)
        )

        // Filler Item
        val fillerItem = CratesMenuYaml.fillerItem

        fillerItem.slots.forEach { slot -> add(slot, Item.fromStack(fillerItem)) }

        // Close Item
        val closeItem = CratePreviewMenuYaml.closeItem

        add(
            closeItem.slot, Item.fromStack(closeItem)
                .click { _ ->
                    close()
                }
        )

        addPaginationControls(pageView)

        show()
    }

    override fun addBorderPage(lastSlot: Int, nextSlot: Int)
    {
        // Controlled manually in build() to avoid depending on framework page(...) state.
    }

    private fun addPaginationControls(pageView: RewardPageView)
    {
        val lastPageItem = CratePreviewMenuYaml.lastPageItem
        val nextPageItem = CratePreviewMenuYaml.nextPageItem

        if (pageView.page > 1)
        {
            add(
                lastPageItem.slot,
                Item.fromStack(lastPageItem)
                    .click { _ ->
                        pageNumber--
                        sound(MenuSound.PAGINATED)
                        build()
                    }
            )
        }

        if (pageView.page < pageView.totalPages)
        {
            add(
                nextPageItem.slot,
                Item.fromStack(nextPageItem)
                    .click { _ ->
                        pageNumber++
                        sound(MenuSound.PAGINATED)
                        build()
                    }
            )
        }
    }

    private fun paginatePreviewRewards(rewards: List<Reward>, pageSlots: List<Int>): RewardPageView
    {
        val base = RewardPagination.paginate(
            rewards = rewards,
            requestedPage = pageNumber,
            pageSlots = pageSlots,
            includeTrailingAddPage = false
        )
        if (base.entries.isEmpty()) return base

        val availableSlots = pageSlots.toMutableList()
        val usedSlots = mutableSetOf<Int>()
        val resolvedEntries = mutableListOf<RewardPageEntry>()

        base.entries.forEach { entry ->
            val customSlot = entry.reward.item.slot
            if (customSlot in pageSlots && customSlot !in usedSlots)
            {
                usedSlots.add(customSlot)
                availableSlots.remove(customSlot)
                resolvedEntries.add(entry.copy(slot = customSlot))
            }
        }

        base.entries.forEach { entry ->
            if (resolvedEntries.any { it.reward.id == entry.reward.id }) return@forEach
            val nextSlot = availableSlots.firstOrNull() ?: return@forEach
            availableSlots.remove(nextSlot)
            usedSlots.add(nextSlot)
            resolvedEntries.add(entry.copy(slot = nextSlot))
        }

        return base.copy(entries = resolvedEntries)
    }
}
