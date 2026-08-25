package com.simplesurvival.crates.menu.keys.manager.link.crates
import com.simplesurvival.crates.util.DisplayNameFormatUtil

import com.simplesurvival.crates.service.key.Key
import com.simplesurvival.lib.menu.api.Menu
import com.simplesurvival.lib.menu.api.item.Item
import com.simplesurvival.lib.menu.api.sound.MenuSound
import org.bukkit.entity.Player

class KeyLinkCratesMenu(
    player: Player,
    val key: Key,
    last: Menu? = null
) : Menu(
    player,
    KeyLinkCratesMenuYaml.title.replace("%id%", key.identifier),
    KeyLinkCratesMenuYaml.rows,
    last,
    KeyLinkCratesMenuYaml.itemsPerPage
)
{

    override fun build()
    {
        clear()

        val unlinkedCrates = key.unlinkedCrates()

        val crateItem = KeyLinkCratesMenuYaml.crateItem

        if (unlinkedCrates.isNotEmpty())
            page(unlinkedCrates, KeyLinkCratesMenuYaml.startItemsPageSlot) { crate, slot ->

                val name = crateItem.rawDisplayName
                    .replace("%name%", DisplayNameFormatUtil.normalize(crate.item.rawDisplayName))
                    .replace("%id%", crate.identifier)

                add(
                    slot, Item.of(crate.item.type)
                        .name(name)
                        .loreStrings(crateItem.rawLore)
                        .click { event ->

                            crate.key.ids.add(key.identifier)
                            crate.update()

                            sound(MenuSound.SUCCESS)

                            if (hasLast())
                                last.build()
                            else
                                close()
                        }
                )

            }

        // Filler
        val fillerItem = KeyLinkCratesMenuYaml.fillerItem

        fillerItem.slots.forEach { slot -> add(slot, Item.fromStack(fillerItem)) }

        // Back Item
        val backItem = KeyLinkCratesMenuYaml.backItem

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
        val lastPageItem = KeyLinkCratesMenuYaml.lastPageItem
        val nextPageItem = KeyLinkCratesMenuYaml.nextPageItem

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


