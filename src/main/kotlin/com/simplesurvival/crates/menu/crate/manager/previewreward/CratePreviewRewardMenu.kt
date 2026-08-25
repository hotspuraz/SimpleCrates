package com.simplesurvival.crates.menu.crate.manager.previewreward

import com.simplesurvival.crates.config.MessagesConfiguration
import com.simplesurvival.crates.service.chat.ChatMessage
import com.simplesurvival.crates.service.chat.ChatMessageKey
import com.simplesurvival.crates.service.chat.ChatMessageService
import com.simplesurvival.crates.service.crate.Crate
import com.simplesurvival.lib.menu.api.Menu
import com.simplesurvival.lib.menu.api.item.Item
import com.simplesurvival.lib.menu.api.sound.MenuSound
import com.simplesurvival.lib.util.kyori.TextUtil
import com.simplesurvival.lib.util.number.NumberUtil
import org.bukkit.entity.Player

class CratePreviewRewardMenu(player: Player, private val crate: Crate, last: Menu?) : Menu(
    player,
    CratePreviewRewardMenuYaml.title.replace("%id%", crate.identifier),
    CratePreviewRewardMenuYaml.rows,
    last
)
{

    init
    {
        allowShift = true
    }

    override fun build()
    {
        clear()

        val textItem = CratePreviewRewardMenuYaml.textItem

        add(
            textItem.slot,
            Item.fromStack(textItem)
                .loreStrings(
                    TextUtil.replacedLoreString(
                        textItem.rawLore,
                        mapOf(
                            Pair("%text%", crate.previewRewardText),
                            Pair("%text_height%", NumberUtil.format(crate.previewRewardTextHeight))
                        )
                    )
                )
                .click { event ->
                    event.isCancelled = true

                    if (event.isLeftClick)
                    {
                        if (event.isShiftClick)
                        {
                            crate.previewRewardTextHeight += 1.0
                            crate.update()

                            sound(MenuSound.SUCCESS)
                            build()
                            return@click
                        }

                        close()
                        sound(MenuSound.SUCCESS)

                        MessagesConfiguration.crateManagerPreviewRewardTextMessage.send(player)
                        ChatMessageService.add(
                            ChatMessage(
                                player.uniqueId,
                                ChatMessageKey.CRATE_PREVIEW_REWARD_TEXT,
                                crate.identifier
                            )
                        )
                        return@click
                    }

                    if (event.isRightClick)
                    {
                        if (event.isShiftClick)
                        {
                            crate.previewRewardTextHeight =
                                (crate.previewRewardTextHeight - 0.1).coerceAtLeast(0.0)
                        } else
                        {
                            crate.previewRewardTextHeight += 0.1
                        }

                        crate.update()
                        sound(MenuSound.SUCCESS)
                        build()
                    }
                }
        )

        val heightItem = CratePreviewRewardMenuYaml.heightItem

        add(
            heightItem.slot,
            Item.fromStack(heightItem)
                .loreStrings(
                    TextUtil.replacedLoreString(
                        heightItem.rawLore,
                        mapOf(
                            Pair("%item_height%", NumberUtil.format(crate.previewRewardItemHeight))
                        )
                    )
                )
                .click { event ->
                    event.isCancelled = true

                    if (event.isLeftClick)
                    {
                        if (event.isShiftClick)
                        {
                            crate.previewRewardItemHeight += 1.0
                        } else
                        {
                            crate.previewRewardItemHeight += 0.1
                        }
                    } else if (event.isRightClick)
                    {
                        if (event.isShiftClick)
                        {
                            crate.previewRewardItemHeight =
                                (crate.previewRewardItemHeight - 1.0).coerceAtLeast(0.0)
                        } else
                        {
                            crate.previewRewardItemHeight =
                                (crate.previewRewardItemHeight - 0.1).coerceAtLeast(0.0)
                        }
                    }

                    crate.update()
                    sound(MenuSound.SUCCESS)
                    build()
                }
        )

        val filler = CratePreviewRewardMenuYaml.fillerItem
        filler.slots.forEach { slot ->
            add(slot, Item.fromStack(filler))
        }

        val back = CratePreviewRewardMenuYaml.backItem
        if (hasLast())
        {
            add(
                back.slot,
                Item.fromStack(back).click { _ ->
                    sound(MenuSound.CHANGE)
                    last.build()
                }
            )
        }

        show()
    }
}
