package com.simplesurvival.crates.menu.crate.manager.rewards.edit.delete

import com.simplesurvival.crates.config.MessagesConfiguration
import com.simplesurvival.crates.menu.crate.manager.rewards.CrateRewardsMenu
import com.simplesurvival.crates.service.crate.Crate
import com.simplesurvival.crates.service.crate.objects.reward.Reward
import com.simplesurvival.lib.menu.api.Menu
import com.simplesurvival.lib.menu.api.item.Item
import com.simplesurvival.lib.menu.api.sound.MenuSound
import org.bukkit.entity.Player

class CrateRewardDeleteMenu(
    player: Player,
    val crate: Crate,
    val reward: Reward,
    last: Menu? = null
) : Menu(
    player,
    CrateRewardDeleteMenuYaml.title.replace("%id%", reward.identifier.take(10)),
    CrateRewardDeleteMenuYaml.rows,
    last
)
{

    override fun build()
    {
        clear()

        val display = CrateRewardDeleteMenuYaml.display

        add(display.slot, Item.fromStack(display))

        val cancel = CrateRewardDeleteMenuYaml.cancel

        cancel.slots.forEach { slot ->
            add(
                slot, Item.fromStack(cancel)
                    .click { event ->

                        sound(MenuSound.ERROR)

                        MessagesConfiguration.crateManagerRewardDeleteCancel.send(player) {
                            it.replace("%id%", reward.identifier)
                        }

                        if (hasLast())
                            last.build()
                        else
                            close()
                    }
            )
        }

        val confirm = CrateRewardDeleteMenuYaml.confirm

        confirm.slots.forEach { slot ->
            add(
                slot, Item.fromStack(confirm)
                    .click { event ->

                        MessagesConfiguration.crateManagerRewardDeleteSuccess.send(player) {
                            it.replace("%id%", reward.identifier)
                        }

                        crate.rewards.removeIf { it.id == reward.id }
                        crate.update()

                        sound(MenuSound.SUCCESS)
                        CrateRewardsMenu(player, crate, false, null).build()
                    }
            )
        }

        show()
    }
}

