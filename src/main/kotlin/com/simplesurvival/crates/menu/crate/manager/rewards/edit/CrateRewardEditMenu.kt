package com.simplesurvival.crates.menu.crate.manager.rewards.edit
import com.simplesurvival.crates.util.DisplayNameFormatUtil

import com.simplesurvival.crates.config.MessagesConfiguration
import com.simplesurvival.crates.menu.crate.manager.rewards.edit.delete.CrateRewardDeleteMenu
import com.simplesurvival.crates.menu.crate.manager.rewards.edit.display.CrateRewardDisplayMenu
import com.simplesurvival.crates.menu.crate.manager.rewards.edit.win.commands.CrateRewardWinCommandsMenu
import com.simplesurvival.crates.menu.crate.manager.rewards.edit.win.items.CrateRewardWinItemsMenu
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
import com.simplesurvival.lib.util.number.NumberUtil
import com.simplesurvival.lib.util.player.PlayerUtil
import org.bukkit.Material
import org.bukkit.entity.Player

class CrateRewardEditMenu(
    player: Player,
    val crate: Crate,
    val reward: Reward,
    last: Menu
) : Menu(
    player,
    CrateRewardEditMenuYaml.title.replace("%id%", reward.identifier.take(10)),
    CrateRewardEditMenuYaml.rows,
    last
)
{

    init
    {
        allowClick = true
        allowShift = true
    }

    override fun build()
    {
        clear()

        // Display Item
        val empty = reward.item.isEmpty

        val displayItem =
            if (empty) CrateRewardEditMenuYaml.displayEmptyItem else CrateRewardEditMenuYaml.displayItem

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
            displayItem.slot,
            (if (empty) Item.of(Material.BARRIER) else Item.fromStack(reward.item.cloneBuilder()))
                .name(displayItem.rawDisplayName)
                .loreStrings(displayLore)
                .click { event ->

                    event.isCancelled = true

                    val cursor = event.cursor

                    if (!cursor.isEmpty)
                    {
                        val previousRewardItem = reward.item.cloneBuilder()
                        val copiedCursorItem = SimpleItemBuilder.clone(cursor)
                        reward.item.updateTo(copiedCursorItem)
                        RewardWinItemSyncUtil.sync(reward, previousRewardItem)
                        crate.update()

                        sound(MenuSound.DONE)
                        build()
                        return@click
                    }

                    if (event.isLeftClick)
                    {

                        sound(MenuSound.CHANGE)
                        CrateRewardDisplayMenu(player, crate, reward, this@CrateRewardEditMenu).build()

                        return@click
                    }

                    if (event.isRightClick)
                    {

                        if (reward.item.isEmpty || PlayerUtil.isInventoryFull(player))
                        {
                            sound(MenuSound.ERROR)
                            return@click
                        }

                        sound(MenuSound.DONE)

                        val normalizedItem = reward.item.cloneBuilder()
                        val normalizedName = DisplayNameFormatUtil.normalize(normalizedItem.rawDisplayName).trim()
                        val normalizedLore = normalizedItem.rawLore.map { DisplayNameFormatUtil.normalize(it) }
                        if (normalizedName.isNotBlank())
                        {
                            normalizedItem.withName(normalizedName)
                        }
                        if (normalizedLore.isNotEmpty())
                        {
                            normalizedItem.withLore(normalizedLore)
                        }

                        player.inventory.addItem(normalizedItem)
                        player.updateInventory()
                    }
                }
        )

        // Win Items
        val winItems = CrateRewardEditMenuYaml.winItems

        add(
            winItems.slot,
            Item.fromStack(winItems)
                .click { event ->

                    event.isCancelled = true

                    sound(MenuSound.CHANGE)
                    CrateRewardWinItemsMenu(player, crate, reward, this@CrateRewardEditMenu).build()
                }
        )

        // Win Commands
        val winCommands = CrateRewardEditMenuYaml.winCommandsItem

        add(
            winCommands.slot,
            Item.fromStack(winCommands)
                .click { event ->

                    event.isCancelled = true

                    sound(MenuSound.CHANGE)
                    CrateRewardWinCommandsMenu(player, crate, reward, this@CrateRewardEditMenu).build()
                }
        )

        // Win Limit

        val currentWinLimit = reward.winLimit

        val winLimit = CrateRewardEditMenuYaml.winLimitItem

        add(
            winLimit.slot,
            Item.fromStack(winLimit)
                .loreStrings(
                    TextUtil.replacedLoreString(
                        winLimit.rawLore,
                        mapOf(
                            Pair(
                                "%win_limit%",
                                if (currentWinLimit <= 0) CrateRewardEditMenuYaml.infiniteSymbol else NumberUtil.formatInt(
                                    currentWinLimit
                                )
                            )
                        )
                    )
                )
                .click { event ->

                    event.isCancelled = true

                    if (event.isLeftClick)
                    {

                        reward.winLimit += if (event.isShiftClick) 10 else 1
                        crate.update()

                        sound(MenuSound.SUCCESS)
                        build()
                    } else if (event.isRightClick)
                    {

                        reward.winLimit = (reward.winLimit - if (event.isShiftClick) 10 else 1)
                            .coerceAtLeast(0)

                        crate.update()

                        sound(MenuSound.SUCCESS)
                        build()
                    }
                }
        )

        // Weight

        val weight = CrateRewardEditMenuYaml.weightItem

        add(
            weight.slot,
            Item.fromStack(weight)
                .loreStrings(
                    TextUtil.replacedLoreString(
                        weight.rawLore,
                        mapOf(
                            Pair(
                                "%weight%",
                                NumberUtil.format(reward.weight)
                            )
                        )
                    )
                )
                .click { event ->

                    event.isCancelled = true

                    if (event.isLeftClick)
                    {
                        reward.weight += if (event.isShiftClick) 10.0 else 0.1
                        crate.update()

                        sound(MenuSound.SUCCESS)
                        build()
                    } else if (event.isRightClick)
                    {
                        reward.weight = (reward.weight - if (event.isShiftClick) 10.0 else 0.1)
                            .coerceAtLeast(0.0)

                        crate.update()

                        sound(MenuSound.SUCCESS)
                        build()
                    }
                }
        )

        // Broadcast

        val broadcast = CrateRewardEditMenuYaml.broadcastItem

        add(
            broadcast.slot,
            Item.fromStack(broadcast)
                .loreStrings(
                    TextUtil.replacedLoreString(
                        broadcast.rawLore,
                        mapOf(
                            Pair(
                                "%is_enabled%",
                                if (reward.broadcastMessageEnabled) "&aYes" else "&cNo"
                            )
                        )
                    )
                )
                .click { event ->
                    event.isCancelled = true

                    reward.broadcastMessageEnabled = !reward.broadcastMessageEnabled
                    crate.update()

                    sound(MenuSound.DONE)
                    build()
                }
        )

        // Restricted Permissions

        val restrictedPermissions = CrateRewardEditMenuYaml.restrictedPermissionsItem

        val restrictedPermissionsLore = mutableListOf<String>()

        restrictedPermissions.rawLore.forEach { line ->
            if (line.contains("%restricted_permissions%"))
            {
                if (reward.restrictedPermissions.isEmpty())
                    restrictedPermissionsLore.add("&7(Empty)")
                else
                    reward.restrictedPermissions.forEach { rawLine ->
                        restrictedPermissionsLore.add(
                            "  &f- $rawLine"
                        )
                    }
            } else
            {
                restrictedPermissionsLore.add(line)
            }
        }

        add(
            restrictedPermissions.slot,
            Item.fromStack(restrictedPermissions)
                .loreStrings(restrictedPermissionsLore)
                .click { event ->

                    event.isCancelled = true

                    if (event.isLeftClick)
                    {

                        close()
                        sound(MenuSound.SUCCESS)

                        MessagesConfiguration.crateManagerRewardRestrictedPermissionsMessage.send(player)

                        ChatMessageService.add(
                            ChatMessage(
                                player.uniqueId,
                                ChatMessageKey.REWARD_RESTRICTED_PERMISSION,
                                crate.identifier,
                                reward.id
                            )
                        )
                    } else if (event.isRightClick)
                    {

                        if (reward.restrictedPermissions.isEmpty())
                        {
                            sound(MenuSound.ERROR)
                            return@click
                        }

                        if (event.isShiftClick)
                            reward.restrictedPermissions.clear()
                        else
                            reward.restrictedPermissions.removeLast()

                        crate.update()

                        sound(MenuSound.DONE)
                        build()
                    }
                }
        )

        // Identifier

        val identifier = CrateRewardEditMenuYaml.identifierItem

        add(
            identifier.slot,
            Item.fromStack(identifier)
                .loreStrings(
                    TextUtil.replacedLoreString(
                        identifier.rawLore,
                        mapOf(
                            Pair(
                                "%id%",
                                reward.identifier
                            )
                        )
                    )
                )
                .click { event ->

                    event.isCancelled = true

                    close()
                    sound(MenuSound.SUCCESS)

                    MessagesConfiguration.crateManagerRewardIdentifierMessage.send(player)

                    ChatMessageService.add(
                        ChatMessage(
                                player.uniqueId,
                                ChatMessageKey.REWARD_IDENTIFIER,
                                crate.identifier,
                                reward.id
                            )
                        )
                }
        )

        // Delete

        val delete = CrateRewardEditMenuYaml.deleteItem

        add(
            delete.slot,
            Item.fromStack(delete)
                .click { event ->

                    event.isCancelled = true

                    sound(MenuSound.CHANGE)
                    CrateRewardDeleteMenu(player, crate, reward, this@CrateRewardEditMenu).build()
                }
        )

        // Filler

        val filler = CrateRewardEditMenuYaml.fillerItem

        filler.slots.forEach { add(it, Item.fromStack(filler), true) }

        // Back

        val back = CrateRewardEditMenuYaml.backItem

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


