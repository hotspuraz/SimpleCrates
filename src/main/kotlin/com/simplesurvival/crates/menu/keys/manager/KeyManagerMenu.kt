package com.simplesurvival.crates.menu.keys.manager
import com.simplesurvival.crates.util.DisplayNameFormatUtil

import com.simplesurvival.crates.config.MessagesConfiguration
import com.simplesurvival.crates.menu.keys.manager.item.KeyItemMenu
import com.simplesurvival.crates.menu.keys.manager.link.KeyLinkMenu
import com.simplesurvival.crates.service.chat.ChatMessage
import com.simplesurvival.crates.service.chat.ChatMessageKey
import com.simplesurvival.crates.service.chat.ChatMessageService
import com.simplesurvival.crates.service.key.Key
import com.simplesurvival.lib.menu.api.Menu
import com.simplesurvival.lib.menu.api.item.Item
import com.simplesurvival.lib.menu.api.sound.MenuSound
import com.simplesurvival.lib.util.kyori.TextUtil
import com.simplesurvival.lib.util.number.NumberUtil
import org.bukkit.entity.Player

class KeyManagerMenu(player: Player, val key: Key, last: Menu?) : Menu(
    player,
    KeyManagerMenuYaml.title.replace("%id%", key.identifier),
    KeyManagerMenuYaml.rows,
    last
)
{

    init
    {
        allowClick = true
    }

    override fun build()
    {
        clear()

        // Identifier
        val identifier = KeyManagerMenuYaml.identifier

        add(
            identifier.slot, Item.fromStack(identifier)
                .loreComponents(
                    TextUtil.replacedLore(
                        identifier.lore(),
                        mapOf(
                            Pair("%id%", key.identifier)
                        )
                    )
                )
                .click { event ->
                    event.isCancelled = true

                    close()
                    sound(MenuSound.SUCCESS)

                    MessagesConfiguration.keyIdentifierMessage.send(player)

                    ChatMessageService.add(
                        ChatMessage(
                            player.uniqueId,
                            ChatMessageKey.KEY_IDENTIFIER,
                            key.identifier
                        )
                    )
                }
        )

        // Key Item
        val keyItem = KeyManagerMenuYaml.keyItem

        val finalLore = mutableListOf<String>()

        keyItem.rawLore.forEach { line ->
            if (line.contains("%lore%"))
            {
                if (key.item.rawLore.isEmpty())
                    finalLore.add("&7(Empty)")
                else
                    key.item.rawLore.forEach { rawLine ->
                        finalLore.add(
                            "  &f- ${rawLine.replace("%name%", DisplayNameFormatUtil.normalize(key.item.rawDisplayName))}"
                        )
                    }
            } else
            {
                finalLore.add(
                    line.replace("%name%", DisplayNameFormatUtil.normalize(key.item.rawDisplayName))
                )
            }
        }

        add(
            keyItem.slot, Item.of(key.item.type)
                .name(TextUtil.parse(keyItem.rawDisplayName))
                .loreStrings(finalLore)
                .click { event ->
                    event.isCancelled = true

                    val cursor = event.cursor

                    if (!cursor.isEmpty)
                    {
                        key.item.updateTo(cursor)
                        key.update()

                        sound(MenuSound.SUCCESS)
                        build()
                        return@click
                    }

                    sound(MenuSound.CHANGE)
                    KeyItemMenu(player, key, this@KeyManagerMenu).build()
                }
        )

        // Crates Item
        val linkCrates = KeyManagerMenuYaml.linkCrates

        add(
            linkCrates.slot, Item.fromStack(linkCrates)
                .loreComponents(
                    TextUtil.replacedLore(
                        linkCrates.lore(),
                        mapOf(
                            Pair("%linked_crates%", NumberUtil.formatInt(key.totalLinkedCrates()))
                        )
                    )
                )
                .click { event ->
                    event.isCancelled = true

                    sound(MenuSound.CHANGE)
                    KeyLinkMenu(player, key, this@KeyManagerMenu).build()
                }
        )

        // Glowing Item
        val glowingItem = KeyManagerMenuYaml.glowingItem

        add(
            glowingItem.slot, Item.fromStack(glowingItem)
                .loreStrings(
                    TextUtil.replacedLoreString(
                        glowingItem.rawLore,
                        mapOf(
                            Pair("%is_enabled%", if (key.glowing) "&aYes" else "&cNo")
                        )
                    )
                )
                .click { event ->
                    event.isCancelled = true

                    key.glowing = !key.glowing
                    key.update()

                    sound(MenuSound.CHANGE)
                    build()
                })

        // Virtual Item
        val virtualItem = KeyManagerMenuYaml.virtual

        add(
            virtualItem.slot, Item.fromStack(virtualItem)
                .loreStrings(
                    TextUtil.replacedLoreString(
                        virtualItem.rawLore,
                        mapOf(
                            Pair("%is_enabled%", if (key.virtual) "&aYes" else "&cNo")
                        )
                    )
                )
                .click { event ->
                    event.isCancelled = true

                    key.virtual = !key.virtual
                    key.update()

                    sound(MenuSound.CHANGE)
                    build()
                })

        // Status Item
        val statusItem =
            if (key.enabled) KeyManagerMenuYaml.enabledItem else KeyManagerMenuYaml.disabledItem

        add(
            statusItem.slot, Item.fromStack(statusItem)
                .click { event ->
                    event.isCancelled = true

                    key.enabled = !key.enabled
                    key.update()

                    sound(MenuSound.DONE)
                    build()
                })

        // Back Item
        val backItem = KeyManagerMenuYaml.backItem

        if (hasLast())
            backItem.slots.forEach {
                add(it, Item.fromStack(backItem).click { event ->
                    event.isCancelled = true

                    sound(MenuSound.CHANGE)
                    last.build()
                })
            }

        // Clone Key Item
        val cloneKey = KeyManagerMenuYaml.cloneKey

        add(
            cloneKey.slot, Item.fromStack(cloneKey)
                .click { event ->
                    event.isCancelled = true

                    val cloned = key.clone()

                    if (cloned == null)
                    {
                        MessagesConfiguration.keyNotCloned.send(player)
                        { it.replace("%key%", key.identifier) }

                        sound(MenuSound.ERROR)
                        return@click
                    }

                    MessagesConfiguration.keyCloned.send(player) {
                        it
                            .replace("%key%", key.identifier)
                            .replace("%id%", cloned.identifier)
                    }

                    sound(MenuSound.SUCCESS)

                    if (hasLast())
                        last.build()
                })

        // Delete Key Item
        val deleteKey = KeyManagerMenuYaml.deleteKey

        add(
            deleteKey.slot, Item.fromStack(deleteKey)
                .click { event ->
                    event.isCancelled = true

                    if (!key.delete())
                    {
                        MessagesConfiguration.keyNotDeleted.send(player)
                        { it.replace("%key%", key.identifier) }

                        sound(MenuSound.ERROR)
                        return@click
                    }

                    MessagesConfiguration.keyDeleted.send(player)
                    { it.replace("%key%", key.identifier) }

                    sound(MenuSound.DONE)

                    if (hasLast())
                        last.build()
                })

        // Filler
        val filler = KeyManagerMenuYaml.filler

        filler.slots.forEach { add(it, Item.fromStack(filler), true) }

        show()
    }
}


