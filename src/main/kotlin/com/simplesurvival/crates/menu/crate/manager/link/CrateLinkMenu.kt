package com.simplesurvival.crates.menu.crate.manager.link
import com.simplesurvival.crates.util.DisplayNameFormatUtil

import com.simplesurvival.crates.config.MessagesConfiguration
import com.simplesurvival.crates.menu.crate.manager.link.keys.CrateLinkKeysMenu
import com.simplesurvival.crates.menu.keys.manager.link.KeyLinkMenuYaml
import com.simplesurvival.crates.service.crate.Crate
import com.simplesurvival.crates.service.profile.CrateProfileService
import com.simplesurvival.lib.menu.api.Menu
import com.simplesurvival.lib.menu.api.item.Item
import com.simplesurvival.lib.menu.api.sound.MenuSound
import com.simplesurvival.lib.util.kyori.TextUtil
import com.simplesurvival.lib.util.player.PlayerUtil
import org.bukkit.entity.Player

class CrateLinkMenu(
    player: Player,
    val crate: Crate,
    last: Menu? = null
) : Menu(
    player,
    CrateLinkMenuYaml.title.replace("%id%", crate.identifier),
    CrateLinkMenuYaml.rows,
    last,
    CrateLinkMenuYaml.itemsPerPage
)
{

    override fun build()
    {
        clear()

        // Keys
        val keyItem = CrateLinkMenuYaml.keyItem

        val initialSlot = CrateLinkMenuYaml.startItemsPageSlot
        var added = initialSlot

        if (crate.linkedKeys().isNotEmpty())
            page(crate.linkedKeys(), initialSlot) { key, slot ->

                val name = keyItem.rawDisplayName
                    .replace("%name%", DisplayNameFormatUtil.normalize(key.item.rawDisplayName))
                    .replace("%id%", key.identifier)

                val lore = TextUtil.replacedLore(
                    keyItem.lore(),
                    mapOf(
                        Pair("%is_enabled%", if (key.enabled) "&aYes" else "&cNo"),
                    )
                )

                add(
                    slot, Item.of(key.item.type)
                        .name(name)
                        .loreComponents(lore)
                        .click { event ->

                            if (event.isLeftClick)
                            {
                                crate.key.ids.remove(key.identifier)
                                crate.update()

                                sound(MenuSound.ERROR)
                                build()
                            } else if (event.isRightClick)
                            {

                                if (!key.virtual)
                                {
                                    if (PlayerUtil.isInventoryFull(player)) {
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

                added++
            }

        // Link
        val linkItem = CrateLinkMenuYaml.linkItem

        add(
            added, Item.fromStack(linkItem)
                .click { event ->
                    sound(MenuSound.CHANGE)
                    CrateLinkKeysMenu(player, crate, this@CrateLinkMenu).build()
                }
        )

        // Requires
        val requiresItem =
            if (crate.key.required) CrateLinkMenuYaml.requiredKeyEnabled else CrateLinkMenuYaml.requiredKeyDisabled

        add(
            requiresItem.slot,
            Item.fromStack(requiresItem)
                .click { event ->
                    crate.key.required = !crate.key.required
                    crate.update()

                    sound(MenuSound.SUCCESS)
                    build()
                }
        )

        // Filler
        val fillerItem = CrateLinkMenuYaml.fillerItem

        fillerItem.slots.forEach { slot -> add(slot, Item.fromStack(fillerItem)) }

        // Back Item
        val backItem = CrateLinkMenuYaml.backItem

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
        val lastPageItem = CrateLinkMenuYaml.lastPageItem
        val nextPageItem = CrateLinkMenuYaml.nextPageItem

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


