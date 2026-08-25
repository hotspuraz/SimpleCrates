package com.simplesurvival.crates.menu.crate.manager.engine.vanilla.block.materials

import com.simplesurvival.crates.service.crate.Crate
import com.simplesurvival.crates.service.crate.objects.engine.types.VanillaBlock
import com.simplesurvival.crates.util.ItemNaming
import com.simplesurvival.crates.util.MaterialOrganizer
import com.simplesurvival.lib.menu.api.Menu
import com.simplesurvival.lib.menu.api.item.Item
import com.simplesurvival.lib.menu.api.sound.MenuSound
import org.bukkit.entity.Player

class CrateVanillaBlockMaterialsMenu(
    player: Player,
    val crate: Crate,
    val engine: VanillaBlock,
    last: Menu? = null
) : Menu(
    player,
    CrateVanillaBlockMaterialsMenuYaml.title.replace("%id%", crate.identifier),
    CrateVanillaBlockMaterialsMenuYaml.rows,
    last,
    CrateVanillaBlockMaterialsMenuYaml.itemsPerPage
)
{

    override fun build()
    {
        clear()

        val solidBlocks = MaterialOrganizer.getOrganizedSolidBlocks()

        val blockItem = CrateVanillaBlockMaterialsMenuYaml.blockItem

        if (solidBlocks.isNotEmpty())
            page(
                solidBlocks, CrateVanillaBlockMaterialsMenuYaml.startItemsPerPage
            ) { material, slot ->

                add(
                    slot, Item.of(material)
                        .name(
                            blockItem.rawDisplayName.replace("%name%", ItemNaming.format(material))
                        )
                        .loreStrings(blockItem.rawLore)
                        .click { event ->

                            engine.material = material
                            crate.updateEngine(engine)

                            sound(MenuSound.SUCCESS)

                            if (hasLast())
                                last.build()
                            else
                                close()
                        }
                )
            }

        // Filler
        val filler = CrateVanillaBlockMaterialsMenuYaml.filler

        filler.slots.forEach { add(it, Item.fromStack(filler)) }

        // Back Item
        val backItem = CrateVanillaBlockMaterialsMenuYaml.backItem

        if (hasLast())
            backItem.slots.forEach {
                add(it, Item.fromStack(backItem).click { event ->
                    sound(MenuSound.CHANGE)
                    last.build()
                })
            }

        show()
    }

    override fun addBorderPage(lastSlot: Int, nextSlot: Int)
    {
        val lastPageItem = CrateVanillaBlockMaterialsMenuYaml.lastPageItem
        val nextPageItem = CrateVanillaBlockMaterialsMenuYaml.nextPageItem

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

