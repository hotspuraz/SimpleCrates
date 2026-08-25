package com.simplesurvival.crates.menu.keys.manager.item
import com.simplesurvival.crates.util.DisplayNameFormatUtil

import com.simplesurvival.crates.config.MessagesConfiguration
import com.simplesurvival.crates.service.chat.ChatMessage
import com.simplesurvival.crates.service.chat.ChatMessageKey
import com.simplesurvival.crates.service.chat.ChatMessageService
import com.simplesurvival.crates.service.key.Key
import com.simplesurvival.lib.menu.api.Menu
import com.simplesurvival.lib.menu.api.item.Item
import com.simplesurvival.lib.menu.api.sound.MenuSound
import com.simplesurvival.lib.util.kyori.TextUtil
import org.bukkit.entity.Player

class KeyItemMenu(player: Player, val key: Key, last: Menu) : Menu(
    player,
    KeyItemMenuYaml.title.replace("%id%", key.identifier),
    KeyItemMenuYaml.rows,
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
        val keyItem = KeyItemMenuYaml.keyItem

        val keyItemLore = mutableListOf<String>()

        keyItem.rawLore.forEach { line ->
            if (line.contains("%lore%"))
            {
                if (key.item.rawLore.isEmpty())
                    keyItemLore.add("&7(Empty)")
                else
                    key.item.rawLore.forEach { rawLine ->
                        keyItemLore.add(
                            "  &f- ${rawLine.replace("%name%", DisplayNameFormatUtil.normalize(key.item.rawDisplayName))}"
                        )
                    }
            } else
            {
                keyItemLore.add(
                    line.replace("%name%", DisplayNameFormatUtil.normalize(key.item.rawDisplayName))
                )
            }
        }

        add(
            keyItem.slot, Item.of(key.item.type)
                .name(TextUtil.parse(keyItem.rawDisplayName))
                .loreStrings(keyItemLore)
                .click { event ->
                    event.isCancelled = true

                    val cursor = event.cursor

                    if (cursor.isEmpty) return@click

                    key.item.updateTo(cursor)
                    key.update()

                    sound(MenuSound.SUCCESS)
                    build()
                }
        )

        // Item Name
        val itemName = KeyItemMenuYaml.itemName

        add(
            itemName.slot, Item.fromStack(itemName)
                .loreStrings(
                    TextUtil.replacedLoreString(
                        itemName.rawLore,
                        mapOf(
                            Pair("%name%", DisplayNameFormatUtil.normalize(key.item.rawDisplayName)),
                        )
                    )
                )
                .click { _ ->

                    close()
                    sound(MenuSound.SUCCESS)

                    MessagesConfiguration.keyManagerItemNameMessage.send(player)

                    ChatMessageService.add(
                        ChatMessage(
                            player.uniqueId,
                            ChatMessageKey.KEY_ITEM_NAME,
                            key.identifier
                        )
                    )
                }
        )

        // Item Lore
        val itemLore = KeyItemMenuYaml.itemLore

        val itemRawLore = mutableListOf<String>()

        itemLore.rawLore.forEach { line ->
            if (line.contains("%lore%"))
            {
                if (key.item.rawLore.isEmpty())
                    itemRawLore.add("&7(Empty)")
                else
                    key.item.rawLore.forEach { rawLine ->
                        itemRawLore.add(
                            "  &f- ${rawLine.replace("%name%", DisplayNameFormatUtil.normalize(key.item.rawDisplayName))}"
                        )
                    }
            } else
            {
                itemRawLore.add(
                    line.replace("%name%", DisplayNameFormatUtil.normalize(key.item.rawDisplayName))
                )
            }
        }

        add(
            itemLore.slot, Item.fromStack(itemLore)
                .loreStrings(itemRawLore)
                .click { event ->

                    if (event.isLeftClick)
                    {
                        close()
                        sound(MenuSound.SUCCESS)

                        MessagesConfiguration.keyManagerItemLoreMessage.send(player)

                        ChatMessageService.add(
                            ChatMessage(
                                player.uniqueId,
                                ChatMessageKey.KEY_ITEM_LORE,
                                key.identifier
                            )
                        )
                    } else if (event.isRightClick)
                    {
                        if (event.isShiftClick)
                        {
                            key.item.rawLore.clear()
                            key.item.withLore(emptyList())
                            key.update()

                            sound(MenuSound.DONE)
                            build()
                            return@click
                        }

                        val lore = key.item.rawLore

                        if (lore.isEmpty())
                        {
                            sound(MenuSound.ERROR)
                            return@click
                        }

                        lore.removeLast()

                        key.item.withLore(lore)
                        key.update()

                        sound(MenuSound.DONE)
                        build()
                    }
                }
        )

        // Filler

        val filler = KeyItemMenuYaml.fillerItem

        filler.slots.forEach { add(it, Item.fromStack(filler)) }

        // Back

        val back = KeyItemMenuYaml.backItem

        if (hasLast())
            add(
                back.slot, Item.fromStack(back)
                    .click { _ ->
                        sound(MenuSound.CHANGE)
                        last.build()
                    })

        show()
    }
}



