package com.simplesurvival.crates.menu.crate.manager.rewards

import com.simplesurvival.crates.service.crate.objects.reward.Reward

data class RewardPageEntry(
    val reward: Reward,
    val globalIndex: Int,
    val slot: Int
)

data class RewardPageView(
    val page: Int,
    val totalPages: Int,
    val entries: List<RewardPageEntry>,
    val pageSlots: List<Int>
)

object RewardPagination
{
    fun rewardSlots(startSlot: Int, itemsPerPage: Int, itemPerRow: Int): List<Int>
    {
        val slots = mutableListOf<Int>()
        var slot = startSlot
        var rowStart = startSlot

        repeat(itemsPerPage) {
            slots.add(slot)
            slot++

            if ((slot - rowStart) == itemPerRow)
            {
                slot += 9 - itemPerRow
                rowStart = slot
            }
        }

        return slots
    }

    fun paginate(
        rewards: List<Reward>,
        requestedPage: Int,
        pageSlots: List<Int>,
        includeTrailingAddPage: Boolean
    ): RewardPageView
    {
        val itemsPerPage = pageSlots.size.coerceAtLeast(1)
        val rewardPages = if (rewards.isEmpty()) 1 else ((rewards.size + itemsPerPage - 1) / itemsPerPage)
        val addPage = includeTrailingAddPage && rewards.isNotEmpty() && rewards.size % itemsPerPage == 0
        val totalPages = rewardPages + if (addPage) 1 else 0
        val page = requestedPage.coerceIn(1, totalPages)
        val startIndex = (page - 1) * itemsPerPage
        val endIndexExclusive = minOf(startIndex + itemsPerPage, rewards.size)

        val entries = mutableListOf<RewardPageEntry>()

        if (startIndex < rewards.size)
        {
            for (globalIndex in startIndex until endIndexExclusive)
            {
                val localIndex = globalIndex - startIndex
                val slot = pageSlots[localIndex]
                entries.add(RewardPageEntry(rewards[globalIndex], globalIndex, slot))
            }
        }

        return RewardPageView(
            page = page,
            totalPages = totalPages,
            entries = entries,
            pageSlots = pageSlots
        )
    }
}
