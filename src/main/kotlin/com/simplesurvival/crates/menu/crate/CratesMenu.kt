package com.simplesurvival.crates.menu.crate
import com.simplesurvival.crates.util.DisplayNameFormatUtil

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.crates.menu.crate.manager.CrateManagerMenu
import com.simplesurvival.crates.service.crate.Crate
import com.simplesurvival.crates.service.crate.CrateService
import com.simplesurvival.crates.service.crate.objects.engine.types.VanillaModel
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import com.simplesurvival.lib.menu.api.Menu
import com.simplesurvival.lib.menu.api.item.Item
import com.simplesurvival.lib.menu.api.sound.MenuSound
import com.simplesurvival.lib.util.kyori.TextUtil
import com.simplesurvival.lib.util.player.PlayerUtil
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

class CratesMenu(player: Player, last: Menu?) : Menu(
    player,
    CratesMenuYaml.title,
    CratesMenuYaml.rows,
    last,
    CratesMenuYaml.itemsPerPage
)
{

    override fun build()
    {
        clear()

        val fillerItem = CratesMenuYaml.fillerItem

        fillerItem.slots.forEach { slot -> add(slot, Item.fromStack(fillerItem)) }

        val crateItem = CratesMenuYaml.crateItem

        val cratesList = CrateService.crates.values.toList()
        val paginatedEntries = mutableListOf<Crate?>().apply {
            addAll(cratesList)
            add(null)
        }

        val initialSlot = CratesMenuYaml.startItemsPageSlot

        page(paginatedEntries, initialSlot) { entry, slot ->

            if (entry == null)
            {
                val addCrateItem = CratesMenuYaml.addCrateItem

                add(
                    slot, Item.fromStack(addCrateItem)
                        .click { event ->
                            if (!event.isLeftClick) return@click

                            val identifier = generateCrateIdentifier()
                            val crate = Crate(
                                uuid = UUID.randomUUID(),
                                identifier = identifier,
                                displayName = identifier,
                                item = SimpleItemBuilder(Material.CHEST)
                            )

                            crate.update()

                            sound(MenuSound.SUCCESS)
                            CrateManagerMenu(player, crate, this@CratesMenu).build()
                        }
                )
                return@page
            }

            val crate = entry

                val name = crateItem.rawDisplayName
                    .replace("%name%", "${DisplayNameFormatUtil.normalize(crate.item.rawDisplayName)}")
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

                            sound(MenuSound.SUCCESS)

                            if (event.isLeftClick)
                                CrateManagerMenu(player, crate, this@CratesMenu).build()
                            else if (event.isRightClick)
                            {

                                if (PlayerUtil.isInventoryFull(player))
                                {
                                    sound(MenuSound.ERROR)
                                    return@click
                                }

                                val clonedCrate = buildCrateGiveItem(crate)

                                clonedCrate.withPersistentDatas(
                                    SimpleItemBuilder.SimplePersistentData(
                                        NamespacedKey(SimpleCrates.plugin, "crate-id"),
                                        PersistentDataType.STRING,
                                        crate.identifier
                                    )
                                )

                                sound(MenuSound.SUCCESS)

                                player.inventory.addItem(clonedCrate)
                                player.updateInventory()
                            }
                        }
                )
        }

        // Back Item
        val backItem = CratesMenuYaml.backItem

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
        val lastPageItem = CratesMenuYaml.lastPageItem
        val nextPageItem = CratesMenuYaml.nextPageItem

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

    private fun generateCrateIdentifier(): String
    {
        var index = 1
        var identifier: String

        do
        {
            identifier = "crate_$index"
            index++
        } while (CrateService.get(identifier) != null)

        return identifier
    }

    private fun buildCrateGiveItem(crate: Crate): SimpleItemBuilder
    {
        val item = crate.item.cloneBuilder()
        item.type = crate.currentBlockMaterial()

        val activeEngine = crate.engine
        if (activeEngine is VanillaModel)
        {
            runCatching {
                val meta = item.itemMeta ?: return@runCatching
                meta.setCustomModelData(activeEngine.customModelData)
                item.itemMeta = meta
            }
        }

        return item
    }
}



