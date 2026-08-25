package com.simplesurvival.crates.menu.crate.manager.rewards.edit.win.commands

import com.simplesurvival.crates.config.MessagesConfiguration
import com.simplesurvival.crates.menu.crate.CratesMenuYaml
import com.simplesurvival.crates.service.chat.ChatMessage
import com.simplesurvival.crates.service.chat.ChatMessageKey
import com.simplesurvival.crates.service.chat.ChatMessageService
import com.simplesurvival.crates.service.crate.Crate
import com.simplesurvival.crates.service.crate.objects.reward.Reward
import com.simplesurvival.lib.menu.api.Menu
import com.simplesurvival.lib.menu.api.item.Item
import com.simplesurvival.lib.menu.api.sound.MenuSound
import com.simplesurvival.lib.util.kyori.TextUtil
import com.simplesurvival.lib.util.number.NumberUtil
import org.bukkit.entity.Player

class CrateRewardWinCommandsMenu(
    player: Player,
    val crate: Crate,
    val reward: Reward,
    last: Menu? = null
) : Menu(
    player,
    CrateRewardWinCommandsMenuYaml.title.replace("%id%", reward.identifier.take(10)),
    CrateRewardWinCommandsMenuYaml.rows,
    last,
    CrateRewardWinCommandsMenuYaml.itemsPerPage
)
{

    override fun build()
    {
        clear()

        val commandItem = CrateRewardWinCommandsMenuYaml.commandItem

        var id = ((pageNumber - 1) * CrateRewardWinCommandsMenuYaml.itemsPerPage) + 1

        val initialSlot = CrateRewardWinCommandsMenuYaml.startItemsPageSlot
        var added = initialSlot
        var localIndex = 0

        if (reward.winCommands.isNotEmpty())
            page(reward.winCommands, initialSlot) { command, slot ->
                val globalIndex = ((pageNumber - 1) * CrateRewardWinCommandsMenuYaml.itemsPerPage) + localIndex

                add(
                    slot, Item.fromStack(commandItem)
                        .name(commandItem.rawDisplayName.replace("%id%", NumberUtil.formatInt(id)))
                        .loreStrings(
                            TextUtil.replacedLoreString(
                                commandItem.rawLore,
                                mapOf(
                                    Pair("%command%", command)
                                )
                            )
                        )
                        .click { event ->

                            if (event.isLeftClick)
                            {

                                close()
                                sound(MenuSound.DONE)

                                MessagesConfiguration.crateManagerRewardWinCommandsMessage.send(player)

                                ChatMessageService.add(
                                    ChatMessage(
                                        player.uniqueId,
                                        ChatMessageKey.REWARD_WIN_COMMAND_EDIT,
                                        crate.identifier,
                                        reward.id,
                                        command
                                    )
                                )
                            } else if (event.isRightClick)
                            {
                                if (globalIndex in reward.winCommands.indices)
                                {
                                    reward.winCommands.removeAt(globalIndex)
                                }
                                crate.update()

                                sound(MenuSound.ERROR)
                                build()
                            }
                        }
                )

                id++
                added++
                localIndex++
            }

        val addCommandItem = CrateRewardWinCommandsMenuYaml.addCommandItem

        add(
            added, Item.fromStack(addCommandItem)
                .click { event ->

                    close()
                    sound(MenuSound.DONE)

                    MessagesConfiguration.crateManagerRewardWinCommandsMessage.send(player)

                    ChatMessageService.add(
                        ChatMessage(
                            player.uniqueId,
                            ChatMessageKey.REWARD_WIN_COMMAND,
                            crate.identifier,
                            reward.id
                        )
                    )
                }
        )

        // Filler Item
        val fillerItem = CrateRewardWinCommandsMenuYaml.fillerItem

        fillerItem.slots.forEach { slot -> add(slot, Item.fromStack(fillerItem)) }

        // Back Item
        val backItem = CrateRewardWinCommandsMenuYaml.backItem

        if (hasLast())
            backItem.slots.forEach { slot ->
                add(slot, Item.fromStack(backItem).click { _ ->
                    sound(MenuSound.CHANGE)
                    last.build()
                })
            }

        show()
    }

    override fun addBorderPage(lastSlot: Int, nextSlot: Int)
    {
        val lastPageItem = CrateRewardWinCommandsMenuYaml.lastPageItem
        val nextPageItem = CrateRewardWinCommandsMenuYaml.nextPageItem

        if (pageNumber > 1)
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

        if (pageNumber < totalPages)
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
}

