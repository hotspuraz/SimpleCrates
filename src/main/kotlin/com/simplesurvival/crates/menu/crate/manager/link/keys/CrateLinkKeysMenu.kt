package com.simplesurvival.crates.menu.crate.manager.link.keys
import com.simplesurvival.crates.util.DisplayNameFormatUtil

import com.simplesurvival.crates.service.crate.Crate
import com.simplesurvival.lib.menu.api.Menu
import com.simplesurvival.lib.menu.api.item.Item
import com.simplesurvival.lib.menu.api.sound.MenuSound
import org.bukkit.entity.Player

class CrateLinkKeysMenu(
    player: Player,
    val crate: Crate,
    last: Menu? = null
) : Menu(
    player,
    CrateLinkKeysMenuYaml.title.replace("%id%", crate.identifier),
    CrateLinkKeysMenuYaml.rows,
    last,
    CrateLinkKeysMenuYaml.itemsPerPage
)
{

    override fun build()
    {
        clear()

        val unlinkedKeys = crate.unlinkedKeys()

        val crateItem = CrateLinkKeysMenuYaml.keyItem

        if (unlinkedKeys.isNotEmpty())
            page(unlinkedKeys, CrateLinkKeysMenuYaml.startItemsPageSlot) { key, slot ->

                val name = crateItem.rawDisplayName
                    .replace("%name%", DisplayNameFormatUtil.normalize(key.item.rawDisplayName))
                    .replace("%id%", key.identifier)

                add(
                    slot, Item.of(key.item.type)
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
        val fillerItem = CrateLinkKeysMenuYaml.fillerItem

        fillerItem.slots.forEach { slot -> add(slot, Item.fromStack(fillerItem)) }

        // Back Item
        val backItem = CrateLinkKeysMenuYaml.backItem

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
        val lastPageItem = CrateLinkKeysMenuYaml.lastPageItem
        val nextPageItem = CrateLinkKeysMenuYaml.nextPageItem

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


