package com.simplesurvival.crates.menu.crate.manager
import com.simplesurvival.crates.util.DisplayNameFormatUtil

import com.simplesurvival.crates.config.MessagesConfiguration
import com.simplesurvival.crates.listener.CrateListener
import com.simplesurvival.crates.menu.crate.manager.engine.vanilla.block.CrateVanillaBlockMenu
import com.simplesurvival.crates.menu.crate.manager.hologram.CrateHologramMenu
import com.simplesurvival.crates.menu.crate.manager.item.CrateItemMenu
import com.simplesurvival.crates.menu.crate.manager.link.CrateLinkMenu
import com.simplesurvival.crates.menu.crate.manager.previewreward.CratePreviewRewardMenu
import com.simplesurvival.crates.menu.crate.manager.rewards.CrateRewardsMenu
import com.simplesurvival.crates.service.chat.ChatMessage
import com.simplesurvival.crates.service.chat.ChatMessageKey
import com.simplesurvival.crates.service.chat.ChatMessageService
import com.simplesurvival.crates.service.crate.Crate
import com.simplesurvival.crates.service.crate.objects.engine.CrateEngineType
import com.simplesurvival.crates.service.crate.objects.engine.types.VanillaBlock
import com.simplesurvival.lib.menu.api.Menu
import com.simplesurvival.lib.menu.api.item.Item
import com.simplesurvival.lib.menu.api.sound.MenuSound
import com.simplesurvival.lib.util.kyori.TextUtil
import com.simplesurvival.lib.util.number.NumberUtil
import org.bukkit.Material
import org.bukkit.entity.Player

class CrateManagerMenu(player: Player, val crate: Crate, last: Menu?) : Menu(
    player,
    CrateManagerMenuYaml.title.replace("%id%", crate.identifier),
    CrateManagerMenuYaml.rows,
    last
)
{

    init
    {
        allowClick = true
    }

    override fun build()
    {
        clear()

        // Identifier
        val identifier = CrateManagerMenuYaml.identifier

        add(
            identifier.slot, Item.fromStack(identifier)
                .loreComponents(
                    TextUtil.replacedLore(
                        identifier.lore(),
                        mapOf(
                            Pair("%id%", crate.identifier)
                        )
                    )
                )
                .click { _ ->

                    close()
                    sound(MenuSound.SUCCESS)

                    MessagesConfiguration.crateIdentifierMessage.send(player)

                    ChatMessageService.add(
                        ChatMessage(
                            player.uniqueId,
                            ChatMessageKey.CRATE_MANAGER_IDENTIFIER,
                            crate.identifier
                        )
                    )
                }
        )

        // Crate Item
        val crateItem = CrateManagerMenuYaml.crateItem

        val finalLore = mutableListOf<String>()

        crateItem.rawLore.forEach { line ->
            if (line.contains("%lore%"))
            {
                if (crate.item.rawLore.isEmpty())
                    finalLore.add("&7(Empty)")
                else
                    crate.item.rawLore.forEach { rawLine ->
                        finalLore.add(
                            "  &f- ${rawLine.replace("%name%", DisplayNameFormatUtil.normalize(crate.item.rawDisplayName))}"
                        )
                    }
            } else
            {
                finalLore.add(
                    line.replace("%name%", DisplayNameFormatUtil.normalize(crate.item.rawDisplayName))
                )
            }
        }

        add(
            crateItem.slot, Item.of(crate.item.type)
                .name(TextUtil.parse(crateItem.rawDisplayName))
                .loreStrings(finalLore)
                .click { event ->
                    event.isCancelled = true

                    val cursor = event.cursor

                    if (!cursor.isEmpty)
                    {
                        crate.item.updateTo(cursor)
                        crate.update()

                        sound(MenuSound.SUCCESS)
                        build()
                        return@click
                    }

                    sound(MenuSound.CHANGE)
                    CrateItemMenu(player, crate, this@CrateManagerMenu).build()
                }
        )

        // Keys Item
        val keysItem = CrateManagerMenuYaml.keys

        add(
            keysItem.slot, Item.fromStack(keysItem)
                .loreComponents(
                    TextUtil.replacedLore(
                        keysItem.lore(),
                        mapOf(
                            Pair("%linked_keys%", NumberUtil.formatInt(crate.key.ids.size)),
                            Pair("%required_key%", if (crate.key.required) "&aYes" else "&cNo")
                        )
                    )
                )
                .click { event ->
                    sound(MenuSound.CHANGE)
                    CrateLinkMenu(player, crate, this@CrateManagerMenu).build()
                }
        )

        // Rewards Item
        val rewardsItem = CrateManagerMenuYaml.rewards

        add(
            rewardsItem.slot, Item.fromStack(rewardsItem)
                .click { _ ->
                    sound(MenuSound.CHANGE)
                    CrateRewardsMenu(player, crate, false, this@CrateManagerMenu).build()
                })

        // Hologram Item
        val hologramItem = CrateManagerMenuYaml.hologram

        add(
            hologramItem.slot, Item.fromStack(hologramItem)
                .click { _ ->
                    sound(MenuSound.CHANGE)
                    CrateHologramMenu(player, crate, this@CrateManagerMenu).build()
                })

        // Engine Item
        val engineItem = CrateManagerMenuYaml.engine

        val engineLore = mutableListOf<String>()

        engineItem.rawLore.forEach { line ->

            if (line.contains("%engines%"))
            {

                if (crate.engines.isEmpty())
                    engineLore.add("&7(Empty)")
                else
                    crate.engines.keys.forEach { engineType ->
                        val color = if (crate.isUsingEngine(engineType)) "&a" else "&7"

                        engineLore.add(" &8- ${color}${engineType.identifier}")
                    }
            } else
                engineLore.add(line)
        }

        add(
            engineItem.slot, Item.fromStack(engineItem)
                .loreStrings(engineLore)
                .click { event ->

                    event.isCancelled = true

                    if (event.isLeftClick)
                    {

                        crate.switchEngine()

                        sound(MenuSound.SUCCESS)
                        build()
                    } else if (event.isRightClick)
                    {

                        sound(MenuSound.CHANGE)

                        when (crate.activeEngine)
                        {
                            CrateEngineType.VANILLA_BLOCK -> CrateVanillaBlockMenu(
                                player,
                                crate,
                                crate.engine as VanillaBlock,
                                this@CrateManagerMenu
                            ).build()

                            CrateEngineType.VANILLA_MODEL ->
                            {
                            }
                        }
                    }

                }
        )

        // Broadcast Message Item
        val broadcastMessageItem = CrateManagerMenuYaml.broadcastMessage

        val broadcastLore = mutableListOf<String>()

        broadcastMessageItem.rawLore.forEach { line ->

            if (line.contains("%broadcast_message%"))
            {
                if (crate.broadcastMessages.isEmpty())
                    broadcastLore.add("&7(Empty)")
                else
                    crate.broadcastMessages.forEach { rawLine ->
                        broadcastLore.add(
                            " &8- $rawLine"
                        )
                    }
            } else
            {
                broadcastLore.add(line)
            }
        }

        add(
            broadcastMessageItem.slot, Item.fromStack(broadcastMessageItem)
                .loreStrings(broadcastLore)
                .click { event ->

                    event.isCancelled = true

                    if (event.isLeftClick)
                    {

                        close()
                        sound(MenuSound.DONE)

                        MessagesConfiguration.crateManageBroadcastMessage.send(player)

                        ChatMessageService.add(
                            ChatMessage(
                                player.uniqueId,
                                ChatMessageKey.CRATE_BROADCAST_MESSAGE,
                                crate.identifier
                            )
                        )
                    } else if (event.isRightClick)
                    {

                        if (crate.broadcastMessages.isEmpty())
                        {
                            sound(MenuSound.ERROR)
                            return@click
                        }

                        if (event.isShiftClick)
                            crate.broadcastMessages.clear()
                        else
                            crate.broadcastMessages.removeLast()

                        crate.update()

                        sound(MenuSound.SUCCESS)
                        build()
                    }
                }
        )

        // Preview Reward Item
        val previewRewardItem =
            if (crate.options.previewReward) CrateManagerMenuYaml.previewRewardEnabled else CrateManagerMenuYaml.previewRewardDisabled

        add(
            previewRewardItem.slot, Item.fromStack(previewRewardItem)
                .click { event ->
                    event.isCancelled = true

                    if (event.isLeftClick)
                    {
                        crate.options.previewReward = !crate.options.previewReward
                        crate.update()

                        sound(MenuSound.SUCCESS)
                        build()
                        return@click
                    }

                    if (event.isRightClick)
                    {
                        sound(MenuSound.CHANGE)
                        CratePreviewRewardMenu(player, crate, this@CrateManagerMenu).build()
                    }
                }
        )

        // Animation Enabled
        val animationItem = CrateManagerMenuYaml.animationEnabled

        add(
            animationItem.slot, Item.fromStack(animationItem)
                .loreStrings(
                    TextUtil.replacedLoreString(
                        animationItem.rawLore,
                        mapOf(
                            Pair("%is_enabled%", if (crate.animationEnabled) "&aYes" else "&cNo")
                        )
                    )
                )
                .click { event ->
                    event.isCancelled = true

                    if (!event.isLeftClick) return@click

                    crate.animationEnabled = !crate.animationEnabled
                    crate.update()

                    sound(MenuSound.SUCCESS)
                    build()
                }
        )

        // Permission Requirement Item
        val permissionRequirementItem = CrateManagerMenuYaml.permissionRequirement

        add(
            permissionRequirementItem.slot, Item.fromStack(permissionRequirementItem)
                .loreStrings(
                    TextUtil.replacedLoreString(
                        permissionRequirementItem.rawLore,
                        mapOf(
                            Pair("%is_required%", if (crate.permission.required) "&aYes" else "&cNo"),
                            Pair("%permission%", crate.permission.key.ifEmpty { "Empty" })
                        )
                    )
                )
                .click { event ->

                    event.isCancelled = true

                    if (event.isLeftClick)
                    {

                        crate.permission.required = !crate.permission.required
                        crate.update()

                        sound(MenuSound.CHANGE)
                        build()
                    } else if (event.isRightClick)
                    {

                        close()
                        sound(MenuSound.DONE)

                        MessagesConfiguration.crateManagePermissionMessage.send(player)

                        ChatMessageService.add(
                            ChatMessage(
                                player.uniqueId,
                                ChatMessageKey.CRATE_PERMISSION_EDIT,
                                crate.identifier
                            )
                        )

                    }
                }
        )
        // Status Item
        val statusItem =
            if (crate.options.enabled) CrateManagerMenuYaml.enabledItem else CrateManagerMenuYaml.disabledItem

        add(
            statusItem.slot, Item.fromStack(statusItem)
                .click { _ ->
                    crate.options.enabled = !crate.options.enabled
                    crate.update()

                    sound(MenuSound.DONE)
                    build()
                })

        // Attack Block Item
        val attachBlockItem = CrateManagerMenuYaml.attackBlock

        attachBlockItem.slots.forEach {
            add(it, Item.fromStack(attachBlockItem).click { _ ->
                close()
                sound(MenuSound.SUCCESS)

                CrateListener.beginAttachBlock(player.uniqueId, crate.identifier)
                MessagesConfiguration.crateAttachBlockMessage.send(player)
            })
        }

        // Back Item
        val backItem = CrateManagerMenuYaml.backItem

        if (hasLast())
            backItem.slots.forEach {
                add(it, Item.fromStack(backItem).click { event ->
                    sound(MenuSound.CHANGE)
                    last.build()
                })
            }

        // Clone Crate Item
        val cloneCrate = CrateManagerMenuYaml.cloneCrate

        add(
            cloneCrate.slot, Item.fromStack(cloneCrate)
                .click { _ ->

                    val cloned = crate.clone()

                    if (cloned == null)
                    {
                        MessagesConfiguration.crateNotCloned.send(player) { it.replace("%crate%", crate.identifier) }

                        sound(MenuSound.ERROR)
                        return@click
                    }

                    MessagesConfiguration.crateCloned.send(player) {
                        it
                            .replace("%crate%", crate.identifier)
                            .replace("%id%", cloned.identifier)
                    }

                    sound(MenuSound.SUCCESS)

                    if (hasLast())
                        last.build()
                })

        // Delete Crate Item
        val deleteCrate = CrateManagerMenuYaml.deleteCrate

        add(
            deleteCrate.slot, Item.fromStack(deleteCrate)
                .click { _ ->

                    if (!crate.delete())
                    {
                        MessagesConfiguration.crateNotDeleted.send(player) { it.replace("%crate%", crate.identifier) }
                        sound(MenuSound.ERROR)
                        return@click
                    }

                    MessagesConfiguration.crateDeleted.send(player) { it.replace("%crate%", crate.identifier) }

                    sound(MenuSound.DONE)

                    if (hasLast())
                        last.build()
                })

        // Filler
        val filler = CrateManagerMenuYaml.filler

        filler.slots.forEach { add(it, Item.fromStack(filler)) }

        show()
    }
}



