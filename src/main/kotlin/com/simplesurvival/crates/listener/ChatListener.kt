package com.simplesurvival.crates.listener

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.crates.config.MessagesConfiguration
import com.simplesurvival.crates.menu.editor.EditorMenu
import com.simplesurvival.crates.menu.crate.CratesMenu
import com.simplesurvival.crates.menu.crate.manager.CrateManagerMenu
import com.simplesurvival.crates.menu.crate.manager.hologram.CrateHologramMenu
import com.simplesurvival.crates.menu.crate.manager.item.CrateItemMenu
import com.simplesurvival.crates.menu.crate.manager.previewreward.CratePreviewRewardMenu
import com.simplesurvival.crates.menu.crate.manager.rewards.CrateRewardsMenu
import com.simplesurvival.crates.menu.crate.manager.rewards.edit.CrateRewardEditMenu
import com.simplesurvival.crates.menu.crate.manager.rewards.edit.display.CrateRewardDisplayMenu
import com.simplesurvival.crates.menu.crate.manager.rewards.edit.win.commands.CrateRewardWinCommandsMenu
import com.simplesurvival.crates.menu.keys.KeysMenu
import com.simplesurvival.crates.menu.keys.manager.KeyManagerMenu
import com.simplesurvival.crates.menu.keys.manager.item.KeyItemMenu
import com.simplesurvival.crates.service.chat.ChatMessageKey
import com.simplesurvival.crates.service.chat.ChatMessageService
import com.simplesurvival.crates.service.crate.CrateService
import com.simplesurvival.crates.service.key.KeyService
import com.simplesurvival.crates.util.DisplayNameFormatUtil
import com.simplesurvival.crates.util.ItemNaming
import com.simplesurvival.crates.util.RewardWinItemSyncUtil
import com.simplesurvival.lib.event.update.UpdateEvent
import com.simplesurvival.lib.util.kyori.TextUtil
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.UUID

class ChatListener : Listener
{
    private val plainTextSerializer = PlainTextComponentSerializer.plainText()
    private val recentAsyncChatHandled: MutableMap<UUID, Long> = ConcurrentHashMap()

    @EventHandler
    fun expires(event: UpdateEvent)
    {
        val toRemove = mutableListOf<UUID>()

        for ((uuid, chat) in ChatMessageService.entriesSnapshot())
        {

            val player = Bukkit.getPlayer(uuid)

            if (chat.hasExpired())
            {

                if (player != null && player.isOnline)
                {
                    MessagesConfiguration.chatExpires.send(player)
                }

                toRemove.add(uuid)
            } else
            {
                val keys = listOf(
                    ChatMessageKey.CRATE_ITEM_LORE,
                    ChatMessageKey.CRATE_HOLOGRAM_LINE,
                    ChatMessageKey.CRATE_PREVIEW_REWARD_TEXT,
                    ChatMessageKey.REWARD_RESTRICTED_PERMISSION,
                    ChatMessageKey.REWARD_DISPLAY_LORE,
                    ChatMessageKey.REWARD_IDENTIFIER
                )

                val subTitles: Map<ChatMessageKey, String> = mapOf(
                    Pair(ChatMessageKey.CRATE_ITEM_LORE, MessagesConfiguration.crateManagerItemLoreSubTitle),
                    Pair(ChatMessageKey.CRATE_HOLOGRAM_LINE, MessagesConfiguration.crateManagerHologramLinesSubTitle),
                    Pair(
                        ChatMessageKey.CRATE_PREVIEW_REWARD_TEXT,
                        MessagesConfiguration.crateManagerPreviewRewardTextSubTitle
                    ),
                    Pair(
                        ChatMessageKey.REWARD_RESTRICTED_PERMISSION,
                        MessagesConfiguration.crateManagerRewardRestrictedPermissionsSubTitle
                    ),
                    Pair(
                        ChatMessageKey.REWARD_DISPLAY_LORE,
                        MessagesConfiguration.crateManagerRewardDisplayLoreSubTitle
                    ),
                    Pair(
                        ChatMessageKey.REWARD_IDENTIFIER,
                        MessagesConfiguration.crateManagerRewardIdentifierSubTitle
                    )
                )

                if (keys.contains(chat.key) && player != null && player.isOnline)
                {

                    val subTitle = subTitles[chat.key]

                    Bukkit.getScheduler().runTask(SimpleCrates.plugin, Runnable {
                        player.showTitle(
                            Title.title(
                                Component.empty(),
                                TextUtil.parse(subTitle),
                                Title.Times.times(
                                    Duration.ZERO,
                                    Duration.ofSeconds(2),
                                    Duration.ZERO
                                )
                            )
                        )
                    })
                }
            }
        }

        toRemove.forEach { ChatMessageService.remove(it) }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun chat(event: AsyncChatEvent)
    {
        val player = event.player
        if (ChatMessageService.get(player.uniqueId) == null) return

        event.isCancelled = true

        val signedRaw = event.signedMessage().message().trim()
        val message = if (signedRaw.isNotBlank())
        {
            signedRaw
        } else
        {
            plainTextSerializer.serialize(event.originalMessage()).trim()
        }
        recentAsyncChatHandled[player.uniqueId] = System.currentTimeMillis()
        processChatInput(player.uniqueId, message)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun chatLegacy(event: AsyncPlayerChatEvent)
    {
        val player = event.player
        if (ChatMessageService.get(player.uniqueId) == null) return

        val lastAsyncHandled = recentAsyncChatHandled[player.uniqueId] ?: 0L
        if (System.currentTimeMillis() - lastAsyncHandled < 500L)
        {
            return
        }

        event.isCancelled = true

        processChatInput(player.uniqueId, event.message.trim())
    }

    private fun processChatInput(playerId: UUID, message: String)
    {
        Bukkit.getScheduler().runTask(SimpleCrates.plugin, Runnable {
            val player = Bukkit.getPlayer(playerId) ?: run {
                ChatMessageService.remove(playerId)
                return@Runnable
            }
            val chat = ChatMessageService.get(playerId) ?: return@Runnable

            if (message.equals("cancel", true) || message.equals("cancelar", true))
            {
                ChatMessageService.remove(player.uniqueId)

                MessagesConfiguration.chatCancel.send(player)
                return@Runnable
            }

            when (chat.key)
            {
                ChatMessageKey.KEY_IDENTIFIER ->
                {
                    val key = KeyService.get(chat.identifier)

                    if (key == null)
                    {
                        MessagesConfiguration.chatKeyNotFound.send(player)
                        ChatMessageService.remove(player.uniqueId)
                        return@Runnable
                    }

                    val newIdentifier = message.lowercase()

                    if (newIdentifier.isBlank())
                    {
                        MessagesConfiguration.keyIdentifierCannotEmpty.send(player)
                        return@Runnable
                    }

                    if (!newIdentifier.matches(Regex("[a-z0-9_-]+")))
                    {
                        MessagesConfiguration.keyIdentifierInvalid.send(player)
                        return@Runnable
                    }

                    if (KeyService.get(newIdentifier) != null)
                    {
                        MessagesConfiguration.keyIdentifierAlreadyExists.send(player) {
                            it.replace("%id%", newIdentifier)
                        }
                        return@Runnable
                    }

                    val oldIdentifier = key.identifier

                    KeyService.renameKey(oldIdentifier, newIdentifier)

                    MessagesConfiguration.keyIdentifierSuccess.send(player) {
                        it
                            .replace("%old%", oldIdentifier)
                            .replace("%new%", newIdentifier)
                    }

                    ChatMessageService.remove(player.uniqueId)

                    KeyManagerMenu(player, key, KeysMenu(player, EditorMenu(player))).build()
                }

                ChatMessageKey.KEY_ITEM_NAME ->
                {
                    val key = KeyService.get(chat.identifier)

                    if (key == null)
                    {
                        MessagesConfiguration.chatKeyNotFound.send(player)
                        ChatMessageService.remove(player.uniqueId)
                        return@Runnable
                    }

                    if (message.isBlank())
                    {
                        MessagesConfiguration.keyManagerItemNameCannotEmpty.send(player)
                        return@Runnable
                    }

                    key.item.withName(message)
                    key.update()

                    MessagesConfiguration.keyManagerItemNameSuccess.send(player) {
                        it.replace(
                            "%name%", DisplayNameFormatUtil.normalize(key.item.rawDisplayName)
                        )
                    }

                    ChatMessageService.remove(player.uniqueId)

                    KeyItemMenu(player, key, KeyManagerMenu(player, key, KeysMenu(player, EditorMenu(player)))).build()
                }

                ChatMessageKey.KEY_ITEM_LORE ->
                {

                    val key = KeyService.get(chat.identifier)

                    if (key == null)
                    {
                        MessagesConfiguration.chatKeyNotFound.send(player)
                        ChatMessageService.remove(player.uniqueId)
                        return@Runnable
                    }

                    if (message.isBlank())
                    {
                        MessagesConfiguration.keyManagerItemLoreCannotEmpty.send(player)
                        return@Runnable
                    }

                    key.item.rawLore.add(message)
                    key.item.withLore(key.item.rawLore)
                    key.update()

                    MessagesConfiguration.keyManagerItemLoreSuccess.send(player)

                    player.resetTitle()
                    ChatMessageService.remove(player.uniqueId)

                    KeyItemMenu(player, key, KeyManagerMenu(player, key, KeysMenu(player, EditorMenu(player)))).build()
                }

                ChatMessageKey.CRATE_MANAGER_IDENTIFIER ->
                {
                    val crate = CrateService.get(chat.identifier)

                    if (crate == null)
                    {
                        MessagesConfiguration.chatCrateNotFound.send(player)
                        ChatMessageService.remove(player.uniqueId)
                        return@Runnable
                    }

                    val newIdentifier = message.lowercase()

                    if (newIdentifier.isBlank())
                    {
                        MessagesConfiguration.crateIdentifierCannotEmpty.send(player)
                        return@Runnable
                    }

                    if (!newIdentifier.matches(Regex("[a-z0-9_-]+")))
                    {
                        MessagesConfiguration.crateIdentifierInvalid.send(player)
                        return@Runnable
                    }

                    if (CrateService.get(newIdentifier) != null)
                    {
                        MessagesConfiguration.crateIdentifierAlreadyExists.send(player) {
                            it.replace("%id%", newIdentifier)
                        }
                        return@Runnable
                    }

                    val oldIdentifier = crate.identifier

                    CrateService.renameCrate(oldIdentifier, newIdentifier)

                    MessagesConfiguration.crateIdentifierSuccess.send(player) {
                        it
                            .replace("%old%", oldIdentifier)
                            .replace("%new%", newIdentifier)
                    }

                    ChatMessageService.remove(player.uniqueId)

                    CrateManagerMenu(player, crate, CratesMenu(player, EditorMenu(player))).build()
                }

                ChatMessageKey.CRATE_ITEM_NAME ->
                {
                    val crate = CrateService.get(chat.identifier)

                    if (crate == null)
                    {
                        MessagesConfiguration.chatCrateNotFound.send(player)
                        ChatMessageService.remove(player.uniqueId)
                        return@Runnable
                    }

                    if (message.isBlank())
                    {
                        MessagesConfiguration.crateManagerItemNameCannotEmpty.send(player)
                        return@Runnable
                    }

                    crate.item.withName(message)
                    crate.update()

                    MessagesConfiguration.crateManagerItemNameSuccess.send(player) {
                        it.replace(
                            "%name%", DisplayNameFormatUtil.normalize(crate.item.rawDisplayName)
                        )
                    }

                    ChatMessageService.remove(player.uniqueId)

                    CrateItemMenu(player, crate, CrateManagerMenu(player, crate, CratesMenu(player, null))).build()
                }

                ChatMessageKey.CRATE_ITEM_LORE ->
                {

                    val crate = CrateService.get(chat.identifier)

                    if (crate == null)
                    {
                        MessagesConfiguration.chatCrateNotFound.send(player)
                        ChatMessageService.remove(player.uniqueId)
                        return@Runnable
                    }

                    if (message.isBlank())
                    {
                        MessagesConfiguration.crateManagerItemLoreCannotEmpty.send(player)
                        return@Runnable
                    }

                    crate.item.rawLore.add(message)
                    crate.item.withLore(crate.item.rawLore)
                    crate.update()

                    MessagesConfiguration.crateManagerItemLoreSuccess.send(player)

                    player.resetTitle()
                    ChatMessageService.remove(player.uniqueId)

                    CrateItemMenu(player, crate, CrateManagerMenu(player, crate, CratesMenu(player, null))).build()
                }

                ChatMessageKey.CRATE_BROADCAST_MESSAGE ->
                {

                    val crate = CrateService.get(chat.identifier)

                    if (crate == null)
                    {
                        MessagesConfiguration.chatCrateNotFound.send(player)
                        ChatMessageService.remove(player.uniqueId)
                        return@Runnable
                    }

                    if (message.isBlank())
                    {
                        MessagesConfiguration.crateManageBroadcastCannotEmpty.send(player)
                        return@Runnable
                    }

                    crate.broadcastMessages.add(message)
                    crate.update()

                    MessagesConfiguration.crateManageBroadcastSuccess.send(player)

                    player.resetTitle()
                    ChatMessageService.remove(player.uniqueId)

                    CrateManagerMenu(player, crate, CratesMenu(player, null)).build()
                }

                ChatMessageKey.CRATE_PERMISSION_EDIT ->
                {

                    val crate = CrateService.get(chat.identifier)

                    if (crate == null)
                    {
                        MessagesConfiguration.chatCrateNotFound.send(player)
                        ChatMessageService.remove(player.uniqueId)
                        return@Runnable
                    }

                    if (message.isBlank())
                    {
                        MessagesConfiguration.crateManagePermissionCannotEmpty.send(player)
                        return@Runnable
                    }

                    crate.permission.key = message
                    crate.update()

                    MessagesConfiguration.crateManagePermissionSuccess.send(player)

                    player.resetTitle()
                    ChatMessageService.remove(player.uniqueId)

                    CrateManagerMenu(player, crate, CratesMenu(player, null)).build()
                }

                ChatMessageKey.CRATE_HOLOGRAM_LINE ->
                {

                    val crate = CrateService.get(chat.identifier)

                    if (crate == null)
                    {
                        MessagesConfiguration.chatCrateNotFound.send(player)
                        ChatMessageService.remove(player.uniqueId)
                        return@Runnable
                    }

                    if (message.isBlank())
                    {
                        MessagesConfiguration.crateManagerHologramLinesCannotEmpty.send(player)
                        return@Runnable
                    }

                    crate.hologram.lines.add(message)
                    crate.update()

                    MessagesConfiguration.crateManagerHologramLinesSuccess.send(player)

                    player.resetTitle()
                    ChatMessageService.remove(player.uniqueId)

                    CrateHologramMenu(player, crate, CrateManagerMenu(player, crate, CratesMenu(player, null))).build()
                }

                ChatMessageKey.CRATE_PREVIEW_REWARD_TEXT ->
                {
                    val crate = CrateService.get(chat.identifier)

                    if (crate == null)
                    {
                        MessagesConfiguration.chatCrateNotFound.send(player)
                        ChatMessageService.remove(player.uniqueId)
                        return@Runnable
                    }

                    if (message.isBlank())
                    {
                        MessagesConfiguration.crateManagerPreviewRewardTextCannotEmpty.send(player)
                        return@Runnable
                    }

                    crate.previewRewardText = message
                    crate.update()

                    MessagesConfiguration.crateManagerPreviewRewardTextSuccess.send(player)

                    player.resetTitle()
                    ChatMessageService.remove(player.uniqueId)

                    CratePreviewRewardMenu(player, crate, CrateManagerMenu(player, crate, CratesMenu(player, null))).build()
                }

                ChatMessageKey.REWARD_DISPLAY_NAME ->
                {
                    val crate = CrateService.get(chat.identifier)

                    if (crate == null)
                    {
                        MessagesConfiguration.chatCrateNotFound.send(player)
                        ChatMessageService.remove(player.uniqueId)
                        return@Runnable
                    }

                    val reward = crate.rewards.find { it.id == chat.rewardId }

                    if (reward == null)
                    {
                        MessagesConfiguration.chatRewardNotFound.send(player)
                        ChatMessageService.remove(player.uniqueId)
                        return@Runnable
                    }

                    if (message.isBlank())
                    {
                        MessagesConfiguration.crateManagerRewardDisplayNameCannotEmpty.send(player)
                        return@Runnable
                    }

                    val previousRewardItem = reward.item.cloneBuilder()
                    reward.item.withName(message)
                    RewardWinItemSyncUtil.sync(reward, previousRewardItem)
                    crate.update()

                    MessagesConfiguration.crateManagerRewardDisplayNameSuccess.send(player) {
                        it.replace(
                            "%name%", ItemNaming.displayNameOrMaterial(reward.item)
                        )
                    }

                    ChatMessageService.remove(player.uniqueId)

                    CrateRewardDisplayMenu(
                        player, crate, reward, CrateRewardEditMenu(
                            player, crate, reward,
                            CrateRewardsMenu(player, crate, false, null)
                        )
                    ).build()
                }

                ChatMessageKey.REWARD_DISPLAY_LORE ->
                {
                    val crate = CrateService.get(chat.identifier)

                    if (crate == null)
                    {
                        MessagesConfiguration.chatCrateNotFound.send(player)
                        ChatMessageService.remove(player.uniqueId)
                        return@Runnable
                    }

                    val reward = crate.rewards.find { it.id == chat.rewardId }

                    if (reward == null)
                    {
                        MessagesConfiguration.chatRewardNotFound.send(player)
                        ChatMessageService.remove(player.uniqueId)
                        return@Runnable
                    }

                    if (message.isBlank())
                    {
                        MessagesConfiguration.crateManagerRewardDisplayLoreCannotEmpty.send(player)
                        return@Runnable
                    }

                    val previousRewardItem = reward.item.cloneBuilder()
                    reward.item.rawLore.add(message)
                    reward.item.withLore(reward.item.rawLore)
                    RewardWinItemSyncUtil.sync(reward, previousRewardItem)
                    crate.update()

                    MessagesConfiguration.crateManagerRewardDisplayLoreSuccess.send(player)

                    player.resetTitle()
                    ChatMessageService.remove(player.uniqueId)

                    CrateRewardDisplayMenu(
                        player, crate, reward, CrateRewardEditMenu(
                            player, crate, reward,
                            CrateRewardsMenu(player, crate, false, null)
                        )
                    ).build()
                }

                ChatMessageKey.REWARD_RESTRICTED_PERMISSION ->
                {
                    val crate = CrateService.get(chat.identifier)

                    if (crate == null)
                    {
                        MessagesConfiguration.chatCrateNotFound.send(player)
                        ChatMessageService.remove(player.uniqueId)
                        return@Runnable
                    }

                    val reward = crate.rewards.find { it.id == chat.rewardId }

                    if (reward == null)
                    {
                        MessagesConfiguration.chatRewardNotFound.send(player)
                        ChatMessageService.remove(player.uniqueId)
                        return@Runnable
                    }

                    if (message.isBlank())
                    {
                        MessagesConfiguration.crateManagerRewardRestrictedPermissionsCannotEmpty.send(player)
                        return@Runnable
                    }

                    if (reward.restrictedPermissions.any { it.equals(message, ignoreCase = true) })
                    {
                        MessagesConfiguration.crateManagerRewardRestrictedPermissionsAlreadyExists.send(player)
                        return@Runnable
                    }

                    reward.restrictedPermissions.add(message)
                    crate.update()

                    MessagesConfiguration.crateManagerRewardRestrictedPermissionsSuccess.send(player) {
                        it.replace("%permission%", message)
                    }

                    player.resetTitle()
                    ChatMessageService.remove(player.uniqueId)

                    CrateRewardEditMenu(
                        player, crate, reward, CrateRewardsMenu(player, crate, false, null)
                    ).build()
                }

                ChatMessageKey.REWARD_IDENTIFIER ->
                {
                    val crate = CrateService.get(chat.identifier)

                    if (crate == null)
                    {
                        MessagesConfiguration.chatCrateNotFound.send(player)
                        ChatMessageService.remove(player.uniqueId)
                        return@Runnable
                    }

                    val reward = crate.rewards.find { it.id == chat.rewardId }

                    if (reward == null)
                    {
                        MessagesConfiguration.chatRewardNotFound.send(player)
                        ChatMessageService.remove(player.uniqueId)
                        return@Runnable
                    }

                    if (message.isBlank())
                    {
                        MessagesConfiguration.crateManagerRewardIdentifierCannotEmpty.send(player)
                        return@Runnable
                    }

                    if (crate.rewards.any { it.id != reward.id && it.identifier.equals(message, ignoreCase = true) })
                    {
                        MessagesConfiguration.crateManagerRewardIdentifierAlreadyExists.send(player)
                        return@Runnable
                    }

                    reward.identifier = message
                    crate.update()

                    MessagesConfiguration.crateManagerRewardIdentifierSuccess.send(player) {
                        it.replace("%identifier%", message)
                    }

                    player.resetTitle()
                    ChatMessageService.remove(player.uniqueId)

                    CrateRewardEditMenu(
                        player, crate, reward, CrateRewardsMenu(player, crate, false, null)
                    ).build()
                }

                ChatMessageKey.REWARD_WIN_COMMAND ->
                {
                    val crate = CrateService.get(chat.identifier)

                    if (crate == null)
                    {
                        MessagesConfiguration.chatCrateNotFound.send(player)
                        ChatMessageService.remove(player.uniqueId)
                        return@Runnable
                    }

                    val reward = crate.rewards.find { it.id == chat.rewardId }

                    if (reward == null)
                    {
                        MessagesConfiguration.chatRewardNotFound.send(player)
                        ChatMessageService.remove(player.uniqueId)
                        return@Runnable
                    }

                    if (message.isBlank())
                    {
                        MessagesConfiguration.crateManagerRewardWinCommandsCannotEmpty.send(player)
                        return@Runnable
                    }

                    SimpleCrates.plugin.logger.info(
                        "[SimpleCrates][RewardCommandSave] action=ADD player='${player.name}' raw='$message'"
                    )

                    if (reward.winCommands.any { it.equals(message, ignoreCase = true) })
                    {
                        MessagesConfiguration.crateManagerRewardWinCommandsAlreadyExists.send(player)
                        return@Runnable
                    }

                    reward.winCommands.add(message)
                    crate.update()

                    MessagesConfiguration.crateManagerRewardWinCommandsSuccess.send(player) {
                        it.replace("%command%", message)
                    }

                    player.resetTitle()
                    ChatMessageService.remove(player.uniqueId)

                    CrateRewardWinCommandsMenu(
                        player,
                        crate,
                        reward,
                        CrateRewardEditMenu(player, crate, reward, CrateRewardsMenu(player, crate, false, null))
                    ).build()
                }

                ChatMessageKey.REWARD_WIN_COMMAND_EDIT ->
                {
                    val crate = CrateService.get(chat.identifier)

                    if (crate == null)
                    {
                        MessagesConfiguration.chatCrateNotFound.send(player)
                        ChatMessageService.remove(player.uniqueId)
                        return@Runnable
                    }

                    val reward = crate.rewards.find { it.id == chat.rewardId }

                    if (reward == null)
                    {
                        MessagesConfiguration.chatRewardNotFound.send(player)
                        ChatMessageService.remove(player.uniqueId)
                        return@Runnable
                    }

                    if (message.isBlank())
                    {
                        MessagesConfiguration.crateManagerRewardWinCommandsCannotEmpty.send(player)
                        return@Runnable
                    }

                    SimpleCrates.plugin.logger.info(
                        "[SimpleCrates][RewardCommandSave] action=EDIT player='${player.name}' old='${chat.otherInformation}' new='$message'"
                    )

                    if (reward.winCommands.any { it.equals(message, ignoreCase = true) })
                    {
                        MessagesConfiguration.crateManagerRewardWinCommandsAlreadyExists.send(player)
                        return@Runnable
                    }

                    reward.winCommands.remove(chat.otherInformation)
                    reward.winCommands.add(message)

                    crate.update()

                    MessagesConfiguration.crateManagerRewardWinCommandsEditSuccess.send(player) {
                        it
                            .replace("%old%", chat.otherInformation)
                            .replace("%new%", message)
                    }

                    player.resetTitle()
                    ChatMessageService.remove(player.uniqueId)

                    CrateRewardWinCommandsMenu(
                        player,
                        crate,
                        reward,
                        CrateRewardEditMenu(player, crate, reward, CrateRewardsMenu(player, crate, false, null))
                    ).build()
                }
            }
        })
    }
}
