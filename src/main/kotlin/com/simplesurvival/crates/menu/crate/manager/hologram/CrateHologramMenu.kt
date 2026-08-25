package com.simplesurvival.crates.menu.crate.manager.hologram

import com.simplesurvival.crates.config.MessagesConfiguration
import com.simplesurvival.crates.service.chat.ChatMessage
import com.simplesurvival.crates.service.chat.ChatMessageKey
import com.simplesurvival.crates.service.chat.ChatMessageService
import com.simplesurvival.crates.service.crate.Crate
import com.simplesurvival.lib.menu.api.Menu
import com.simplesurvival.lib.menu.api.item.Item
import com.simplesurvival.lib.menu.api.sound.MenuSound
import com.simplesurvival.lib.util.kyori.TextUtil
import com.simplesurvival.lib.util.number.NumberUtil
import org.bukkit.entity.Player

class CrateHologramMenu(player: Player, val crate: Crate, last: Menu?) : Menu(
    player,
    CrateHologramMenuYaml.title.replace("%id%", crate.identifier),
    CrateHologramMenuYaml.rows,
    last
)
{

    init
    {
        allowShift = true
    }

    override fun build()
    {
        clear()

        val linesItem = CrateHologramMenuYaml.linesItem

        val linesLore = mutableListOf<String>()

        linesItem.rawLore.forEach { line ->
            if (line.contains("%lines%"))
            {
                if (crate.hologram.lines.isEmpty())
                    linesLore.add("&7(Empty)")
                else
                    crate.hologram.lines.forEach { rawLine ->
                        linesLore.add(
                            "&8- $rawLine"
                        )
                    }
            } else
            {
                linesLore.add(line)
            }
        }

        add(
            linesItem.slot, Item.fromStack(linesItem)
                .loreStrings(linesLore)
                .click { event ->

                    if (event.isLeftClick)
                    {

                        if (event.isShiftClick)
                        {

                            crate.hologram.lines.add("")
                            crate.update()

                            sound(MenuSound.DONE)
                            build()
                            return@click
                        }

                        close()
                        sound(MenuSound.SUCCESS)

                        MessagesConfiguration.crateManagerHologramLinesMessage.send(player)

                        ChatMessageService.add(
                            ChatMessage(
                                player.uniqueId,
                                ChatMessageKey.CRATE_HOLOGRAM_LINE,
                                crate.identifier
                            )
                        )
                    } else if (event.isRightClick)
                    {

                        if (event.isShiftClick)
                        {

                            crate.hologram.lines.clear()
                            crate.update()

                            sound(MenuSound.DONE)
                            build()
                            return@click
                        }

                        crate.hologram.lines.removeLast()
                        crate.update()

                        sound(MenuSound.SUCCESS)
                        build()
                    }
                }
        )

        // Offset
        val offSetItem = CrateHologramMenuYaml.offSetItem

        add(
            offSetItem.slot, Item.fromStack(offSetItem)
                .loreStrings(
                    TextUtil.replacedLoreString(
                        offSetItem.rawLore,
                        mapOf(
                            Pair("%offset%", NumberUtil.format(crate.hologram.offset))
                        )
                    )
                )
                .click { event ->

                    if (event.isLeftClick)
                    {

                        if (event.isShiftClick)
                        {

                            crate.hologram.offset += 1.0
                            crate.update()

                            sound(MenuSound.SUCCESS)
                            build()
                            return@click
                        }

                        crate.hologram.offset += 0.1
                        crate.update()

                        sound(MenuSound.SUCCESS)
                        build()
                    } else if (event.isRightClick)
                    {

                        if (event.isShiftClick)
                        {

                            crate.hologram.offset = (crate.hologram.offset - 1.0).coerceAtLeast(0.0)
                            crate.update()

                            sound(MenuSound.SUCCESS)
                            build()
                            return@click
                        }

                        crate.hologram.offset -= 0.1
                        crate.update()

                        sound(MenuSound.SUCCESS)
                        build()
                    }
                }
        )

        // Filler

        val filler = CrateHologramMenuYaml.fillerItem

        filler.slots.forEach { add(it, Item.fromStack(filler)) }

        // Back

        val back = CrateHologramMenuYaml.backItem

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

