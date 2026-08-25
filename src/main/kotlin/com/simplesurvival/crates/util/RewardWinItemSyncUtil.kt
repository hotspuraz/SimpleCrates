package com.simplesurvival.crates.util

import com.simplesurvival.crates.service.crate.objects.reward.Reward
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder

object RewardWinItemSyncUtil
{

    fun sync(reward: Reward, previousRewardItem: SimpleItemBuilder? = null)
    {
        if (reward.winItems.isEmpty())
        {
            reward.winItems.add(reward.item.cloneBuilder())
            return
        }

        if (previousRewardItem == null) return

        val previousStack = previousRewardItem.cloneBuilder()
        val updatedStack = reward.item.cloneBuilder()

        for (index in reward.winItems.indices)
        {
            val winItem = reward.winItems[index]
            if (winItem.cloneBuilder() == previousStack)
            {
                reward.winItems[index] = updatedStack.cloneBuilder()
            }
        }
    }
}
