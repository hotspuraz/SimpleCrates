package com.simplesurvival.crates.menu.keys
import com.simplesurvival.crates.util.DisplayNameFormatUtil

import com.simplesurvival.crates.config.MessagesConfiguration
import com.simplesurvival.crates.menu.keys.manager.KeyManagerMenu
import com.simplesurvival.crates.service.key.Key
import com.simplesurvival.crates.service.key.KeyService
import com.simplesurvival.crates.service.profile.CrateProfileService
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import com.simplesurvival.lib.menu.api.Menu
import com.simplesurvival.lib.menu.api.item.Item
import com.simplesurvival.lib.menu.api.sound.MenuSound
import com.simplesurvival.lib.util.kyori.TextUtil
import com.simplesurvival.lib.util.number.NumberUtil
import com.simplesurvival.lib.util.player.PlayerUtil
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.UUID
import kotlin.text.replace

class KeysMenu(player: Player, last: Menu?) : Menu(
    player,
    KeysMenuYaml.title,
    KeysMenuYaml.rows,
    last,
    KeysMenuYaml.itemsPerPage
)
{

    override fun build()
    {
        clear()

        val fillerItem = KeysMenuYaml.fillerItem

        fillerItem.slots.forEach { slot -> add(slot, Item.fromStack(fillerItem)) }

        val keyItem = KeysMenuYaml.keyItem

        val keyList = KeyService.keyMap.values.toList()
        val paginatedEntries = mutableListOf<Key?>().apply {
            addAll(keyList)
            add(null)
        }

        val initialSlot = KeysMenuYaml.startItemsPageSlot

        page(paginatedEntries, initialSlot) { entry, slot ->

            if (entry == null)
            {
                val addKeyItem = KeysMenuYaml.addKeyItem

                add(
                    slot, Item.fromStack(addKeyItem)
                        .click { event ->
                            if (!event.isLeftClick) return@click

                            val identifier = generateKeyIdentifier()
                            val key = Key(
                                uuid = UUID.randomUUID(),
                                identifier = identifier,
                                item = SimpleItemBuilder(Material.TRIPWIRE_HOOK)
                            )

                            KeyService.addKey(key)

                            sound(MenuSound.SUCCESS)
                            KeyManagerMenu(player, key, this@KeysMenu).build()
                        }
                )
                return@page
            }

            val key = entry

                val name = keyItem.rawDisplayName
                    .replace("%name%", DisplayNameFormatUtil.normalize(key.item.rawDisplayName))
                    .replace("%id%", key.identifier)

                val lore = TextUtil.replacedLore(
                    keyItem.lore(),
                    mapOf(
                        Pair("%is_enabled%", if (key.enabled) "&aYes" else "&cNo"),
                        Pair("%crates_linked%", NumberUtil.formatInt(key.totalLinkedCrates()))
                    )
                )

                add(
                    slot, Item.of(key.item.type)
                        .name(name)
                        .loreComponents(lore)
                        .click { event ->

                            if (event.isLeftClick)
                            {
                                sound(MenuSound.CHANGE)
                                KeyManagerMenu(player, key, this@KeysMenu).build()
                            } else if (event.isRightClick)
                            {

                                if (!key.virtual)
                                {

                                    if (PlayerUtil.isInventoryFull(player))
                                    {
                                        sound(MenuSound.ERROR)
                                        return@click
                                    }

                                    sound(MenuSound.SUCCESS)

                                    player.inventory.addItem(key.item)
                                    player.updateInventory()
                                } else
                                {

                                    sound(MenuSound.SUCCESS)

                                    val profile = CrateProfileService.instance.read(player.uniqueId)

                                    profile.addKey(key.identifier)
                                    MessagesConfiguration.keyGiveMessage.send(player) {
                                        it.replace("%name%", DisplayNameFormatUtil.normalize(key.item.rawDisplayName))
                                    }
                                }
                            }
                        }
                )
        }

        // Back Item
        val backItem = KeysMenuYaml.backItem

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
        val lastPageItem = KeysMenuYaml.lastPageItem
        val nextPageItem = KeysMenuYaml.nextPageItem

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

    private fun generateKeyIdentifier(): String
    {
        var index = 1
        var identifier: String

        do
        {
            identifier = "key_$index"
            index++
        } while (KeyService.get(identifier) != null)

        return identifier
    }
}


