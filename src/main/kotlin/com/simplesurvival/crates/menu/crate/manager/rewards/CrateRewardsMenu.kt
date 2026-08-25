package com.simplesurvival.crates.menu.crate.manager.rewards

import com.simplesurvival.crates.menu.crate.manager.rewards.edit.CrateRewardEditMenu
import com.simplesurvival.crates.service.crate.Crate
import com.simplesurvival.crates.service.crate.objects.reward.Reward
import com.simplesurvival.lib.menu.api.Menu
import com.simplesurvival.lib.menu.api.item.Item
import com.simplesurvival.lib.menu.api.sound.MenuSound
import com.simplesurvival.lib.util.kyori.TextUtil
import com.simplesurvival.lib.util.number.NumberUtil
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.Collections

class CrateRewardsMenu(
    player: Player,
    val crate: Crate,
    var reorderEnabled: Boolean = false,
    last: Menu? = null
) : Menu(
    player,
    CrateRewardsMenuYaml.title.replace("%id%", crate.identifier),
    CrateRewardsMenuYaml.rows,
    last,
    CrateRewardsMenuYaml.itemsPerPage,
)
{
    private var selectedRewardId: String? = null

    init
    {
        allowShift = true
        allowClickItemWithQuantity = true
    }

    override fun build()
    {
        clear()

        if (!reorderEnabled && selectedRewardId != null)
        {
            selectedRewardId = null
            player.setItemOnCursor(null)
        }

        val rewards = crate.rewards
        val pageSlots = RewardPagination.rewardSlots(
            startSlot = CrateRewardsMenuYaml.startItemsPageSlot,
            itemsPerPage = CrateRewardsMenuYaml.itemsPerPage,
            itemPerRow = itemPerRow
        )

        val pageView = RewardPagination.paginate(
            rewards = rewards,
            requestedPage = pageNumber,
            pageSlots = pageSlots,
            includeTrailingAddPage = !reorderEnabled
        )

        if (pageNumber != pageView.page)
        {
            pageNumber = pageView.page
        }

        val totalWeight = rewards.sumOf { it.weight }

        pageView.entries.forEach { entry ->
            val reward = entry.reward
            val rewardId = reward.id
            val empty = reward.item.isEmpty
            val globalNumber = entry.globalIndex + 1

            val rewardItem =
                if (empty) CrateRewardsMenuYaml.rewardInvalidItem else CrateRewardsMenuYaml.rewardItem

            val chance = if (totalWeight > 0.0)
                (reward.weight / totalWeight) * 100.0
            else 0.0

            add(
                entry.slot,
                (if (!empty) Item.fromStack(reward.item.cloneBuilder()) else Item.of(Material.BARRIER))
                    .name(rewardItem.rawDisplayName.replace("%id%", globalNumber.toString()))
                    .loreStrings(
                        TextUtil.replacedLoreString(
                            if (!reorderEnabled) rewardItem.rawLore else CrateRewardsMenuYaml.rewardReorderLore,
                            mapOf(
                                Pair("%weight%", NumberUtil.format(reward.weight)),
                                Pair("%chance%", "${NumberUtil.format(chance)}%")
                            )
                        )
                    )
                    .click { event ->

                        if (reorderEnabled)
                        {
                            handleReorderClick(rewardId)
                            return@click
                        }

                        val currentReward = crate.rewards.firstOrNull { it.id == rewardId }
                        if (currentReward == null)
                        {
                            sound(MenuSound.ERROR)
                            build()
                            return@click
                        }

                        if (event.isLeftClick)
                        {
                            sound(MenuSound.CHANGE)
                            CrateRewardEditMenu(player, crate, currentReward, this@CrateRewardsMenu).build()
                        } else if (event.isRightClick)
                        {
                            val removed = crate.rewards.removeIf { it.id == rewardId }
                            if (!removed)
                            {
                                sound(MenuSound.ERROR)
                                build()
                                return@click
                            }

                            crate.update()
                            sound(MenuSound.ERROR)
                            build()
                        }
                    }
            )
        }

        if (!reorderEnabled)
        {
            val addSlot = pageView.pageSlots.getOrNull(pageView.entries.size)
            if (addSlot != null)
            {
                val addRewardItem = CrateRewardsMenuYaml.addRewardItem
                add(
                    addSlot,
                    Item.fromStack(addRewardItem)
                        .click { _ ->
                            val reward = Reward()
                            crate.rewards.add(reward)
                            crate.update()

                            sound(MenuSound.SUCCESS)
                            CrateRewardEditMenu(player, crate, reward, this@CrateRewardsMenu).build()
                        }
                )
            }
        }

        // Reorder
        val reorderItem = CrateRewardsMenuYaml.reorderItem

        add(
            reorderItem.slot, Item.fromStack(reorderItem)
                .loreStrings(
                    TextUtil.replacedLoreString(
                        reorderItem.rawLore,
                        mapOf(
                            Pair("%is_enabled%", if (reorderEnabled) "&aYes" else "&cNo")
                        )
                    )
                )
                .click { _ ->
                    reorderEnabled = !reorderEnabled
                    if (!reorderEnabled)
                    {
                        selectedRewardId = null
                        player.setItemOnCursor(null)
                    }

                    sound(MenuSound.CHANGE)
                    build()
                }
        )

        // Max Win
        val maxWinItem = CrateRewardsMenuYaml.maxWinItem

        add(
            maxWinItem.slot, Item.fromStack(maxWinItem)
                .loreStrings(
                    TextUtil.replacedLoreString(
                        maxWinItem.rawLore,
                        mapOf(
                            Pair(
                                "%max_win_rewards%",
                                "${NumberUtil.formatInt(CrateRewardsMenuYaml.currentMaxWinRewards)}/${
                                    NumberUtil.formatInt(
                                        CrateRewardsMenuYaml.maxWinRewards
                                    )
                                }"
                            )
                        )
                    )
                )
                .click { event ->

                    if (event.isLeftClick)
                    {
                        if (event.isShiftClick)
                        {
                            CrateRewardsMenuYaml.currentMaxWinRewards =
                                (CrateRewardsMenuYaml.currentMaxWinRewards + 10).coerceAtMost(CrateRewardsMenuYaml.maxWinRewards)
                            CrateRewardsMenuYaml.save()

                            sound(MenuSound.SUCCESS)
                            build()
                            return@click
                        }

                        CrateRewardsMenuYaml.currentMaxWinRewards =
                            (CrateRewardsMenuYaml.currentMaxWinRewards + 1).coerceAtMost(CrateRewardsMenuYaml.maxWinRewards)
                        CrateRewardsMenuYaml.save()

                        sound(MenuSound.SUCCESS)
                        build()
                    } else if (event.isRightClick)
                    {
                        if (event.isShiftClick)
                        {
                            CrateRewardsMenuYaml.currentMaxWinRewards =
                                (CrateRewardsMenuYaml.currentMaxWinRewards - 10).coerceAtLeast(0)
                            CrateRewardsMenuYaml.save()

                            sound(MenuSound.SUCCESS)
                            build()
                            return@click
                        }

                        CrateRewardsMenuYaml.currentMaxWinRewards =
                            (CrateRewardsMenuYaml.currentMaxWinRewards - 1).coerceAtLeast(0)
                        CrateRewardsMenuYaml.save()

                        sound(MenuSound.SUCCESS)
                        build()
                    }
                }
        )

        addPaginationControls(pageView)

        // Filler
        val filler = CrateRewardsMenuYaml.fillerItem
        filler.slots.forEach { add(it, Item.fromStack(filler)) }

        // Back
        val back = CrateRewardsMenuYaml.backItem
        if (hasLast())
            add(
                back.slot, Item.fromStack(back)
                    .click { _ ->
                        sound(MenuSound.CHANGE)
                        last.build()
                    })

        show()
    }

    override fun addBorderPage(lastSlot: Int, nextSlot: Int)
    {
        // Controlled manually in build() to avoid depending on framework page(...) state.
    }

    private fun addPaginationControls(pageView: RewardPageView)
    {
        val lastPageItem = CrateRewardsMenuYaml.lastPageItem
        val nextPageItem = CrateRewardsMenuYaml.nextPageItem

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

    private fun handleReorderClick(rewardId: String)
    {
        val selected = selectedRewardId

        if (selected == null)
        {
            selectedRewardId = rewardId
            val reward = crate.rewards.firstOrNull { it.id == rewardId }
            player.setItemOnCursor(reward?.item?.cloneBuilder())

            sound(MenuSound.PAGINATED)
            build()
            return
        }

        if (selected == rewardId)
        {
            selectedRewardId = null
            player.setItemOnCursor(null)

            sound(MenuSound.ERROR)
            build()
            return
        }

        val firstIndex = crate.rewards.indexOfFirst { it.id == selected }
        val secondIndex = crate.rewards.indexOfFirst { it.id == rewardId }

        if (firstIndex < 0 || secondIndex < 0)
        {
            selectedRewardId = null
            player.setItemOnCursor(null)
            sound(MenuSound.ERROR)
            build()
            return
        }

        Collections.swap(crate.rewards, firstIndex, secondIndex)

        selectedRewardId = null
        player.setItemOnCursor(null)
        crate.update()

        sound(MenuSound.SUCCESS)
        build()
    }
}
