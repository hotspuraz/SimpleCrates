package com.simplesurvival.crates.menu.crate.manager.rewards.edit.win.items

import com.simplesurvival.crates.menu.crate.manager.rewards.edit.win.commands.CrateRewardWinCommandsMenuYaml
import com.simplesurvival.crates.service.crate.Crate
import com.simplesurvival.crates.service.crate.objects.reward.Reward
import com.simplesurvival.crates.util.DisplayNameFormatUtil
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import com.simplesurvival.lib.menu.api.Menu
import com.simplesurvival.lib.menu.api.item.Item
import com.simplesurvival.lib.menu.api.sound.MenuSound
import com.simplesurvival.lib.util.player.PlayerUtil
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class CrateRewardWinItemsMenu(
    player: Player,
    val crate: Crate,
    val reward: Reward,
    last: Menu? = null
) : Menu(
    player,
    CrateRewardWinItemsMenuYaml.title.replace("%id%", reward.identifier.take(10)),
    CrateRewardWinItemsMenuYaml.rows,
    last,
    CrateRewardWinItemsMenuYaml.itemsPerPage
)
{

    init
    {
        allowClick = true
        checkCursorItem = true
        allowClickItemWithQuantity = true
    }

    override fun withCursor(cursor: ItemStack)
    {
        super.withCursor(cursor)

        sound(MenuSound.PAGINATED)
        build()

        player.setItemOnCursor(cursor)
    }

    override fun withoutCursor()
    {
        super.withoutCursor()

        sound(MenuSound.ERROR)
        build()
    }

    override fun build()
    {
        clear()

        if (!withCursorItem)
        {
            val displayItem = CrateRewardWinItemsMenuYaml.itemDisplay

            val initialSlot = CrateRewardWinItemsMenuYaml.startItemsPageSlot
            var added = initialSlot
            var localIndex = 0

            if (reward.winItems.isNotEmpty())
                page(reward.winItems, initialSlot) { item, slot ->
                    val globalIndex = ((pageNumber - 1) * CrateRewardWinItemsMenuYaml.itemsPerPage) + localIndex

                    val lore: MutableList<String> = mutableListOf()

                    lore.addAll(item.rawLore.map { DisplayNameFormatUtil.normalize(it) })
                    lore.addAll(displayItem.rawLore)

                    add(
                        slot, Item.fromStack(normalized(item))
                            .loreStrings(lore)
                            .click { event ->
                                event.isCancelled = true

                                if (event.isLeftClick)
                                {

                                    if (PlayerUtil.isInventoryFull(player))
                                    {
                                        sound(MenuSound.ERROR)
                                        return@click
                                    }

                                    sound(MenuSound.SUCCESS)

                                    player.inventory.addItem(normalized(item))
                                    player.updateInventory()
                                } else if (event.isRightClick)
                                {
                                    if (globalIndex in reward.winItems.indices)
                                    {
                                        reward.winItems.removeAt(globalIndex)
                                    }
                                    crate.update()

                                    sound(MenuSound.ERROR)
                                    build()
                                }
                            }
                    )

                    added++
                    localIndex++
                }

            // Add Item
            val addItem = CrateRewardWinItemsMenuYaml.addItem

            add(added, Item.fromStack(addItem))
        } else
        {

            val addFiller = CrateRewardWinItemsMenuYaml.addFiller

            addFiller.slots.forEach { slot ->
                add(
                    slot,
                    Item.fromStack(addFiller)
                        .click { event ->
                            event.isCancelled = true

                            val cursor = event.cursor

                            if (cursor.isEmpty)
                            {
                                sound(MenuSound.ERROR)
                                return@click
                            }

                            reward.winItems.add(SimpleItemBuilder.clone(cursor))
                            crate.update()

                            player.setItemOnCursor(null)
                            player.updateInventory()

                            this.withCursorItem = false

                            sound(MenuSound.SUCCESS)
                            build()
                        }
                )
            }
        }

        // Filler Item
        val fillerItem = CrateRewardWinItemsMenuYaml.fillerItem

        fillerItem.slots.forEach { slot -> add(slot, Item.fromStack(fillerItem)) }

        // Back Item
        val backItem = CrateRewardWinItemsMenuYaml.backItem

        if (hasLast())
            backItem.slots.forEach { slot ->
                add(slot, Item.fromStack(backItem).click { _ ->
                    sound(MenuSound.CHANGE)
                    last.build()
                })
            }

        show()
    }

    private fun normalized(source: SimpleItemBuilder): SimpleItemBuilder
    {
        val item = source.cloneBuilder()
        val normalizedName = DisplayNameFormatUtil.normalize(item.rawDisplayName).trim()
        val normalizedLore = item.rawLore.map { DisplayNameFormatUtil.normalize(it) }
        if (normalizedName.isNotBlank())
        {
            item.withName(normalizedName)
        }
        if (normalizedLore.isNotEmpty())
        {
            item.withLore(normalizedLore)
        }
        return item
    }
}

