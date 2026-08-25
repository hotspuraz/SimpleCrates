package com.simplesurvival.crates.menu.keys.manager.link
import com.simplesurvival.crates.util.DisplayNameFormatUtil

import com.simplesurvival.crates.menu.crate.manager.CrateManagerMenu
import com.simplesurvival.crates.menu.keys.manager.link.crates.KeyLinkCratesMenu
import com.simplesurvival.crates.service.key.Key
import com.simplesurvival.lib.menu.api.Menu
import com.simplesurvival.lib.menu.api.item.Item
import com.simplesurvival.lib.menu.api.sound.MenuSound
import com.simplesurvival.lib.util.kyori.TextUtil
import org.bukkit.entity.Player

class KeyLinkMenu(
    player: Player,
    val key: Key,
    last: Menu? = null
) : Menu(
    player,
    KeyLinkMenuYaml.title.replace("%id%", key.identifier),
    KeyLinkMenuYaml.rows,
    last,
    KeyLinkMenuYaml.itemsPerPage
)
{

    override fun build()
    {
        clear()

        // Crates
        val crateItem = KeyLinkMenuYaml.crateItem

        val initialSlot = KeyLinkMenuYaml.startItemsPageSlot
        var added = initialSlot

        if (key.linkedCrates().isNotEmpty())
            page(key.linkedCrates(), initialSlot) { crate, slot ->

                val name = crateItem.rawDisplayName
                    .replace("%name%", DisplayNameFormatUtil.normalize(crate.item.rawDisplayName))
                    .replace("%id%", crate.identifier)

                val lore = TextUtil.replacedLore(
                    crateItem.lore(),
                    mapOf(
                        Pair("%is_enabled%", if (crate.options.enabled) "&aYes" else "&cNo"),
                    )
                )

                add(
                    slot, Item.of(crate.item.type)
                        .name(name)
                        .loreComponents(lore)
                        .click { event ->

                            crate.key.ids.remove(key.identifier)
                            crate.update()

                            sound(MenuSound.ERROR)
                            build()
                        }
                )

                added++
            }

        // Link
        val linkItem = KeyLinkMenuYaml.linkItem

        add(
            added, Item.fromStack(linkItem)
                .click { event ->
                    sound(MenuSound.CHANGE)
                    KeyLinkCratesMenu(player, key, this@KeyLinkMenu).build()
                }
        )

        // Filler
        val fillerItem = KeyLinkMenuYaml.fillerItem

        fillerItem.slots.forEach { slot -> add(slot, Item.fromStack(fillerItem)) }

        // Back Item
        val backItem = KeyLinkMenuYaml.backItem

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
        val lastPageItem = KeyLinkMenuYaml.lastPageItem
        val nextPageItem = KeyLinkMenuYaml.nextPageItem

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


