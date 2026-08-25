package com.simplesurvival.crates.menu.editor

import com.simplesurvival.lib.menu.api.Menu
import com.simplesurvival.lib.menu.api.item.Item
import com.simplesurvival.lib.menu.api.sound.MenuSound
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class EditorMenu(player: Player) : Menu(player, EditorMenuYaml.title, EditorMenuYaml.rows)
{
    override fun build()
    {
        clear()

        EditorMenuYaml.items().forEach { (_, itemBuilder) ->
            val item = Item.fromStack(itemBuilder).click { event ->

                val success = itemBuilder.handleWithAction(event)

                if (success)
                    sound(MenuSound.SUCCESS)
            }

            itemBuilder.slots.forEach { slot ->
                add(slot, item)
            }
        }

        show()
    }
}

