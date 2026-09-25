package com.simplesurvival.crates.menu.crate.manager.rewards.edit.display
import com.simplesurvival.crates.util.DisplayNameFormatUtil

import com.simplesurvival.crates.config.MessagesConfiguration
import com.simplesurvival.crates.service.chat.ChatMessage
import com.simplesurvival.crates.service.chat.ChatMessageKey
import com.simplesurvival.crates.service.chat.ChatMessageService
import com.simplesurvival.crates.service.crate.Crate
import com.simplesurvival.crates.service.crate.objects.reward.Reward
import com.simplesurvival.crates.util.ItemNaming
import com.simplesurvival.crates.util.RewardWinItemSyncUtil
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import com.simplesurvival.lib.menu.api.Menu
import com.simplesurvival.lib.menu.api.item.Item
import com.simplesurvival.lib.menu.api.sound.MenuSound
import com.simplesurvival.lib.util.kyori.TextUtil
import org.bukkit.Material
import org.bukkit.entity.Player

class CrateRewardDisplayMenu(
    player: Player,
    val crate: Crate,
    val reward: Reward,
    last: Menu
) : Menu(
    player,
    CrateRewardDisplayMenuYaml.title.replace("%id%", crate.identifier),
    CrateRewardDisplayMenuYaml.rows,
    last
)
{

    init
    {
        allowShift = true
        allowClick = true
    }

    override fun build()
    {
        clear()

        // Crate
        val empty = reward.item.isEmpty

        val displayItem = if (!empty) CrateRewardDisplayMenuYaml.display else CrateRewardDisplayMenuYaml.displayEmpty

        val displayLore = mutableListOf<String>()

        displayItem.rawLore.forEach { line ->
            if (line.contains("%lore%"))
            {
                if (reward.item.rawLore.isEmpty())
                    displayLore.add("&7(Empty)")
                else
                    reward.item.rawLore.forEach { rawLine ->
                        val normalizedLoreLine = DisplayNameFormatUtil.normalize(rawLine)
                        displayLore.add(
                            "  &f- ${
                                normalizedLoreLine.replace(
                                    "%name%",
                                    ItemNaming.displayNameOrMaterial(reward.item)
                                )
                            }"
                        )
                    }
            } else
            {
                displayLore.add(
                    line.replace("%name%", ItemNaming.displayNameOrMaterial(reward.item))
                )
            }
        }

        add(
            displayItem.slot, (if (!empty) Item.fromStack(reward.item.cloneBuilder()) else Item.of(Material.BARRIER))
                .name(TextUtil.parse(displayItem.rawDisplayName))
                .loreStrings(displayLore)
                .click { event ->
                    event.isCancelled = true

                    val cursor = event.cursor

                    if (cursor.isEmpty) return@click

                    val previousRewardItem = reward.item.cloneBuilder()
                    val copiedCursorItem = SimpleItemBuilder.clone(cursor)
                    reward.item.updateTo(copiedCursorItem)
                    RewardWinItemSyncUtil.sync(reward, previousRewardItem)
                    crate.update()

                    sound(MenuSound.SUCCESS)
                    build()
                }
        )

        // Item Name
        val rewardName = CrateRewardDisplayMenuYaml.rewardName

        add(
            rewardName.slot, Item.fromStack(rewardName)
                .loreStrings(
                    TextUtil.replacedLoreString(
                        rewardName.rawLore,
                        mapOf(
                            Pair("%name%", ItemNaming.displayNameOrMaterial(reward.item)),
                        )
                    )
                )
                .click { event ->
                    event.isCancelled = true

                    close()
                    sound(MenuSound.SUCCESS)

                    MessagesConfiguration.crateManagerRewardDisplayNameMessage.send(player)

                    ChatMessageService.add(
                        ChatMessage(
                            player.uniqueId,
                            ChatMessageKey.REWARD_DISPLAY_NAME,
                            crate.identifier,
                            reward.id
                        )
                    )
                }
        )

        // Item Lore
        val rewardLore = CrateRewardDisplayMenuYaml.rewardLore

        val itemRawLore = mutableListOf<String>()

        rewardLore.rawLore.forEach { line ->
            if (line.contains("%lore%"))
            {
                if (reward.item.rawLore.isEmpty())
                    itemRawLore.add("&7(Empty)")
                else
                    reward.item.rawLore.forEach { rawLine ->
                        val normalizedLoreLine = DisplayNameFormatUtil.normalize(rawLine)
                        itemRawLore.add(
                            "  &f- ${
                                normalizedLoreLine.replace(
                                    "%name%",
                                    ItemNaming.displayNameOrMaterial(reward.item)
                                )
                            }"
                        )
                    }
            } else
            {
                itemRawLore.add(
                    line.replace("%name%", ItemNaming.displayNameOrMaterial(reward.item))
                )
            }
        }

        add(
            rewardLore.slot, Item.fromStack(rewardLore)
                .loreStrings(itemRawLore)
                .click { event ->
                    event.isCancelled = true

                    if (event.isLeftClick)
                    {
                        close()
                        sound(MenuSound.SUCCESS)

                        MessagesConfiguration.crateManagerRewardDisplayLoreMessage.send(player)

                        ChatMessageService.add(
                            ChatMessage(
                                player.uniqueId,
                                ChatMessageKey.REWARD_DISPLAY_LORE,
                                crate.identifier,
                                reward.id
                            )
                        )
                    } else if (event.isRightClick)
                    {
                        if (event.isShiftClick)
                        {
                            val previousRewardItem = reward.item.cloneBuilder()
                            reward.item.rawLore.clear()
                            reward.item.withLore(emptyList())
                            RewardWinItemSyncUtil.sync(reward, previousRewardItem)
                            crate.update()

                            sound(MenuSound.DONE)
                            build()
                            return@click
                        }

                        val lore = reward.item.rawLore

                        if (lore.isEmpty())
                        {
                            sound(MenuSound.ERROR)
                            return@click
                        }

                        val previousRewardItem = reward.item.cloneBuilder()
                        lore.removeLast()

                        reward.item.withLore(lore)
                        RewardWinItemSyncUtil.sync(reward, previousRewardItem)
                        crate.update()

                        sound(MenuSound.DONE)
                        build()
                    }
                }
        )

        // Filler

        val filler = CrateRewardDisplayMenuYaml.fillerItem

        filler.slots.forEach { add(it, Item.fromStack(filler), true) }

        // Back

        val back = CrateRewardDisplayMenuYaml.backItem

        if (hasLast())
            add(
                back.slot, Item.fromStack(back)
                    .click { event ->
                        event.isCancelled = true

                        sound(MenuSound.CHANGE)
                        last.build()
                    })

        show()
    }
}



