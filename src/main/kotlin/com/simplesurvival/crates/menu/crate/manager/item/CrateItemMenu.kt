package com.simplesurvival.crates.menu.crate.manager.item
import com.simplesurvival.crates.util.DisplayNameFormatUtil

import com.simplesurvival.crates.config.MessagesConfiguration
import com.simplesurvival.crates.service.chat.ChatMessage
import com.simplesurvival.crates.service.chat.ChatMessageKey
import com.simplesurvival.crates.service.chat.ChatMessageService
import com.simplesurvival.crates.service.crate.Crate
import com.simplesurvival.lib.menu.api.Menu
import com.simplesurvival.lib.menu.api.item.Item
import com.simplesurvival.lib.menu.api.sound.MenuSound
import com.simplesurvival.lib.util.kyori.TextUtil
import org.bukkit.entity.Player

class CrateItemMenu(player: Player, val crate: Crate, last: Menu) : Menu(
    player,
    CrateItemMenuYaml.title.replace("%id%", crate.identifier),
    CrateItemMenuYaml.rows,
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
        val crateItem = CrateItemMenuYaml.crateItem

        val crateItemLore = mutableListOf<String>()

        crateItem.rawLore.forEach { line ->
            if (line.contains("%lore%"))
            {
                if (crate.item.rawLore.isEmpty())
                    crateItemLore.add("&7(Empty)")
                else
                    crate.item.rawLore.forEach { rawLine ->
                        crateItemLore.add(
                            "  &f- ${rawLine.replace("%name%", DisplayNameFormatUtil.normalize(crate.item.rawDisplayName))}"
                        )
                    }
            } else
            {
                crateItemLore.add(
                    line.replace("%name%", DisplayNameFormatUtil.normalize(crate.item.rawDisplayName))
                )
            }
        }

        add(
            crateItem.slot, Item.of(crate.item.type)
                .name(TextUtil.parse(crateItem.rawDisplayName))
                .loreStrings(crateItemLore)
                .click { event ->
                    event.isCancelled = true

                    val cursor = event.cursor

                    if (cursor.isEmpty) return@click

                    crate.item.updateTo(cursor)
                    crate.update()

                    sound(MenuSound.SUCCESS)
                    build()
                }
        )

        // Item Name
        val itemName = CrateItemMenuYaml.itemName

        add(
            itemName.slot, Item.fromStack(itemName)
                .loreStrings(
                    TextUtil.replacedLoreString(
                        itemName.rawLore,
                        mapOf(
                            Pair("%name%", DisplayNameFormatUtil.normalize(crate.item.rawDisplayName)),
                        )
                    )
                )
                .click { event ->
                    event.isCancelled = true

                    close()
                    sound(MenuSound.SUCCESS)

                    MessagesConfiguration.crateManagerItemNameMessage.send(player)

                    ChatMessageService.add(
                        ChatMessage(
                            player.uniqueId,
                            ChatMessageKey.CRATE_ITEM_NAME,
                            crate.identifier
                        )
                    )
                }
        )

        // Item Lore
        val itemLore = CrateItemMenuYaml.itemLore

        val itemRawLore = mutableListOf<String>()

        itemLore.rawLore.forEach { line ->
            if (line.contains("%lore%"))
            {
                if (crate.item.rawLore.isEmpty())
                    itemRawLore.add("&7(Empty)")
                else
                    crate.item.rawLore.forEach { rawLine ->
                        itemRawLore.add(
                            "  &f- ${rawLine.replace("%name%", DisplayNameFormatUtil.normalize(crate.item.rawDisplayName))}"
                        )
                    }
            } else
            {
                itemRawLore.add(
                    line.replace("%name%", DisplayNameFormatUtil.normalize(crate.item.rawDisplayName))
                )
            }
        }

        add(
            itemLore.slot, Item.fromStack(itemLore)
                .loreStrings(itemRawLore)
                .click { event ->
                    event.isCancelled = true

                    if (event.isLeftClick)
                    {
                        close()
                        sound(MenuSound.SUCCESS)

                        MessagesConfiguration.crateManagerItemLoreMessage.send(player)

                        ChatMessageService.add(
                            ChatMessage(
                                player.uniqueId,
                                ChatMessageKey.CRATE_ITEM_LORE,
                                crate.identifier
                            )
                        )
                    } else if (event.isRightClick)
                    {
                        if (event.isShiftClick)
                        {
                            crate.item.rawLore.clear()
                            crate.item.withLore(emptyList())
                            crate.update()

                            sound(MenuSound.DONE)
                            build()
                            return@click
                        }

                        val lore = crate.item.rawLore

                        if (lore.isEmpty())
                        {
                            sound(MenuSound.ERROR)
                            return@click
                        }

                        lore.removeLast()

                        crate.item.withLore(lore)
                        crate.update()

                        sound(MenuSound.DONE)
                        build()
                    }
                }
        )

        // Filler

        val filler = CrateItemMenuYaml.fillerItem

        filler.slots.forEach { add(it, Item.fromStack(filler), true) }

        // Back

        val back = CrateItemMenuYaml.backItem

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



