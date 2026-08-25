package com.simplesurvival.crates.menu.crate.manager.engine.vanilla.block

import com.simplesurvival.crates.menu.crate.manager.CrateManagerMenuYaml
import com.simplesurvival.crates.menu.crate.manager.engine.vanilla.block.materials.CrateVanillaBlockMaterialsMenu
import com.simplesurvival.crates.service.crate.Crate
import com.simplesurvival.crates.service.crate.CrateService
import com.simplesurvival.crates.service.crate.objects.engine.types.VanillaBlock
import com.simplesurvival.crates.util.ItemNaming
import com.simplesurvival.lib.menu.api.Menu
import com.simplesurvival.lib.menu.api.item.Item
import com.simplesurvival.lib.menu.api.sound.MenuSound
import com.simplesurvival.lib.util.kyori.TextUtil
import org.bukkit.entity.Player

class CrateVanillaBlockMenu(
    player: Player,
    val crate: Crate,
    val engine: VanillaBlock,
    last: Menu? = null
) : Menu(
    player,
    CrateVanillaBlockMenuYaml.title.replace("%id%", crate.identifier),
    CrateVanillaBlockMenuYaml.rows,
    last
)
{

    override fun build()
    {
        clear()

        val blockItem = CrateVanillaBlockMenuYaml.blockMaterial

        add(
            blockItem.slot, Item.of(engine.material)
                .name(blockItem.rawDisplayName)
                .loreStrings(
                    TextUtil.replacedLoreString(
                        blockItem.rawLore,
                        mapOf(
                            Pair("%material%", ItemNaming.format(engine.material))
                        )
                    )
                )
                .click { event ->

                    if (event.isLeftClick)
                    {

                        // Open material's menu

                        sound(MenuSound.CHANGE)
                        CrateVanillaBlockMaterialsMenu(player, crate, engine, this@CrateVanillaBlockMenu).build()
                    } else if (event.isRightClick)
                    {

                        // Update block
                        crate.updateEngine(engine)
                        CrateService.updatePlacedBlocks(crate)

                        sound(MenuSound.SUCCESS)
                        build()
                    }
                }
        )

        // Filler
        val filler = CrateVanillaBlockMenuYaml.filler

        filler.slots.forEach { add(it, Item.fromStack(filler)) }

        // Back Item
        val backItem = CrateVanillaBlockMenuYaml.backItem

        if (hasLast())
            backItem.slots.forEach {
                add(it, Item.fromStack(backItem).click { event ->
                    sound(MenuSound.CHANGE)
                    last.build()
                })
            }

        show()
    }
}


