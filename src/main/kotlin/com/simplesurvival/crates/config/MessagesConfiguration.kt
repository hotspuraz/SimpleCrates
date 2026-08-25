package com.simplesurvival.crates.config

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.lib.configuration.key.SimpleKey
import com.simplesurvival.lib.configuration.serializer.types.message.MessageWrapper
import com.simplesurvival.lib.configuration.yaml.YamlBuilder
import com.simplesurvival.lib.util.caps.SmallCapsConverter.format

object MessagesConfiguration : YamlBuilder<SimpleCrates>(SimpleCrates.plugin, "messages.yml")
{

    @field:SimpleKey(node = "messages.chat.expires")
    var chatExpires = MessageWrapper("&cIt took you too long to type. Please try again later.")

    @field:SimpleKey(node = "messages.chat.cancel")
    var chatCancel = MessageWrapper("&cYou have canceled your chat entry.")

    @field:SimpleKey(node = "messages.chat.crate-not-found")
    var chatCrateNotFound = MessageWrapper("&cThe crate you were editing could not be found.")

    @field:SimpleKey(node = "messages.chat.key-not-found")
    var chatKeyNotFound = MessageWrapper("&cThe key you were editing could not be found.")

    @field:SimpleKey(node = "messages.chat.reward-not-found")
    var chatRewardNotFound = MessageWrapper("&cThe reward you were editing could not be found.")

    @field:SimpleKey(node = "messages.key.manager.delete.fail")
    var keyNotDeleted = MessageWrapper("&cThe %key% key could not be deleted!")

    @field:SimpleKey(node = "messages.key.manager.delete.success")
    var keyDeleted = MessageWrapper("&eThe %key% key has been successfully deleted.")

    @field:SimpleKey(node = "messages.key.manager.clone.fail")
    var keyNotCloned = MessageWrapper("&cThe %key% key could not be cloned!")

    @field:SimpleKey(node = "messages.key.manager.clone.success")
    var keyCloned = MessageWrapper("&aThe %key% key has been successfully cloned. ID: &7%id%")

    @field:SimpleKey(node = "messages.key.manager.item.lore.message")
    var keyManagerItemLoreMessage =
        MessageWrapper("&eWrite the new line in the chat.\n&aTo exit this session type &e\"cancel\"&a.")

    @field:SimpleKey(node = "messages.key.manager.item.lore.sub-title")
    var keyManagerItemLoreSubTitle = "&a${format("Write in the chat.")}"

    @field:SimpleKey(node = "messages.key.manager.item.lore.success")
    var keyManagerItemLoreSuccess = MessageWrapper("&aLine successfully added!")

    @field:SimpleKey(node = "messages.key.manager.item.lore.cannot-empty")
    var keyManagerItemLoreCannotEmpty = MessageWrapper("&cThe item lore cannot be empty.")

    @field:SimpleKey(node = "messages.key.manager.item.name.message")
    var keyManagerItemNameMessage =
        MessageWrapper("&aType the new item name in the chat. To cancel, type '&ecancel&a'")

    @field:SimpleKey(node = "messages.key.manager.item.name.cannot-empty")
    var keyManagerItemNameCannotEmpty = MessageWrapper("&cThe item name cannot be empty.")

    @field:SimpleKey(node = "messages.key.manager.item.name.already-exists")
    var keyManagerItemNameAlreadyExists = MessageWrapper("&cThe item name already exists.")

    @field:SimpleKey(node = "messages.key.manager.item.name.success")
    var keyManagerItemNameSuccess = MessageWrapper("&aThe item name has been updated to %name%")

    @field:SimpleKey(node = "messages.key.manager.identifier.message")
    var keyIdentifierMessage =
        MessageWrapper("&aType the new key identifier in the chat. To cancel, type '&ecancel&a'")

    @field:SimpleKey(node = "messages.key.manager.identifier.cannot-empty")
    var keyIdentifierCannotEmpty = MessageWrapper("&cThe key identifier cannot be empty.")

    @field:SimpleKey(node = "messages.key.manager.identifier.invalid")
    var keyIdentifierInvalid = MessageWrapper("&cUse only letters, numbers, _, and -.")

    @field:SimpleKey(node = "messages.key.manager.identifier.already-exists")
    var keyIdentifierAlreadyExists = MessageWrapper("&cA key with the ID %id% already exists. Please try again.")

    @field:SimpleKey(node = "messages.key.manager.identifier.success")
    var keyIdentifierSuccess = MessageWrapper("&aThe key identifier has been changed from %old% to %new%.")

    @field:SimpleKey(node = "messages.key.manager.link.give-key")
    var keyGiveMessage = MessageWrapper("&aYou have received the key %name%&a.")

    @field:SimpleKey(node = "messages.crate.manager.reward.win-commands.message")
    var crateManagerRewardWinCommandsMessage =
        MessageWrapper("&eWrite the new command in the chat.\n&aTo exit this session type &e\"cancel\"&a.")

    @field:SimpleKey(node = "messages.crate.manager.reward.win-commands.success")
    var crateManagerRewardWinCommandsSuccess =
        MessageWrapper("&aThe command '&e%command%&a' has been added!")

    @field:SimpleKey(node = "messages.crate.manager.reward.win-commands.edit-success")
    var crateManagerRewardWinCommandsEditSuccess =
        MessageWrapper("&aThe command '&e%old%&a' has been edited to '&e%new%&a'!")

    @field:SimpleKey(node = "messages.crate.manager.reward.win-commands.already-exists")
    var crateManagerRewardWinCommandsAlreadyExists =
        MessageWrapper("&cThe command already exists!")

    @field:SimpleKey(node = "messages.crate.manager.reward.win-commands.cannot-empty")
    var crateManagerRewardWinCommandsCannotEmpty =
        MessageWrapper("&cThe command cannot be empty.")

    @field:SimpleKey(node = "messages.crate.manager.reward.identifier.message")
    var crateManagerRewardIdentifierMessage =
        MessageWrapper("&eWrite the new identifier in the chat.\n&aTo exit this session type &e\"cancel\"&a.")

    @field:SimpleKey(node = "messages.crate.manager.reward.identifier.cannot-empty")
    var crateManagerRewardIdentifierCannotEmpty =
        MessageWrapper("&cThe reward identifier cannot be empty.")

    @field:SimpleKey(node = "messages.crate.manager.reward.identifier.already-exists")
    var crateManagerRewardIdentifierAlreadyExists =
        MessageWrapper("&cA reward with this identifier already exists.")

    @field:SimpleKey(node = "messages.crate.manager.reward.identifier.success")
    var crateManagerRewardIdentifierSuccess =
        MessageWrapper("&aReward identifier changed to '&e%identifier%&a' successfully.")

    @field:SimpleKey(node = "messages.crate.manager.reward.identifier.sub-title")
    var crateManagerRewardIdentifierSubTitle = "&a${format("Write in the chat.")}"

    @field:SimpleKey(node = "messages.crate.manager.reward.restricted-permissions.message")
    var crateManagerRewardRestrictedPermissionsMessage =
        MessageWrapper("&eWrite the new permission in the chat.\n&aTo exit this session type &e\"cancel\"&a.")

    @field:SimpleKey(node = "messages.crate.manager.reward.restricted-permissions.cannot-empty")
    var crateManagerRewardRestrictedPermissionsCannotEmpty = MessageWrapper("&cThe permission cannot be empty.")

    @field:SimpleKey(node = "messages.crate.manager.reward.restricted-permissions.already-exists")
    var crateManagerRewardRestrictedPermissionsAlreadyExists = MessageWrapper("&cThe permission already exists.")

    @field:SimpleKey(node = "messages.crate.manager.reward.restricted-permissions.success")
    var crateManagerRewardRestrictedPermissionsSuccess =
        MessageWrapper("&aThe permission '&e%permission%&a' has been added.")

    @field:SimpleKey(node = "messages.crate.manager.reward.restricted-permissions.sub-title")
    var crateManagerRewardRestrictedPermissionsSubTitle = "&a${format("Write in the chat.")}"

    @field:SimpleKey(node = "messages.crate.manager.reward.delete.success")
    var crateManagerRewardDeleteSuccess = MessageWrapper("&eThe reward '&b%id%&e' has been deleted.")

    @field:SimpleKey(node = "messages.crate.manager.reward.delete.cancel")
    var crateManagerRewardDeleteCancel = MessageWrapper("&cYou have given up deleting the '&e%id%&c' reward")

    @field:SimpleKey(node = "messages.crate.manager.hologram.lines.message")
    var crateManagerHologramLinesMessage =
        MessageWrapper("&eWrite the new line in the chat.\n&aTo exit this session type &e\"cancel\"&a.")

    @field:SimpleKey(node = "messages.crate.manager.hologram.lines.sub-title")
    var crateManagerHologramLinesSubTitle = "&a${format("Write in the chat.")}"

    @field:SimpleKey(node = "messages.crate.manager.hologram.lines.success")
    var crateManagerHologramLinesSuccess = MessageWrapper("&aLine successfully added!")

    @field:SimpleKey(node = "messages.crate.manager.hologram.lines.cannot-empty")
    var crateManagerHologramLinesCannotEmpty = MessageWrapper("&cThe line cannot be empty.")

    @field:SimpleKey(node = "messages.crate.manager.preview-reward.text.message")
    var crateManagerPreviewRewardTextMessage =
        MessageWrapper("&eWrite the new preview reward text in chat.\n&aTo exit this session type &e\"cancel\"&a.")

    @field:SimpleKey(node = "messages.crate.manager.preview-reward.text.sub-title")
    var crateManagerPreviewRewardTextSubTitle = "&a${format("Write in the chat.")}"

    @field:SimpleKey(node = "messages.crate.manager.preview-reward.text.success")
    var crateManagerPreviewRewardTextSuccess = MessageWrapper("&aPreview reward text updated successfully.")

    @field:SimpleKey(node = "messages.crate.manager.preview-reward.text.cannot-empty")
    var crateManagerPreviewRewardTextCannotEmpty = MessageWrapper("&cThe preview reward text cannot be empty.")

    @field:SimpleKey(node = "messages.crate.manager.reward.display.lore.message")
    var crateManagerRewardDisplayLoreMessage =
        MessageWrapper("&eWrite the new line in the chat.\n&aTo exit this session type &e\"cancel\"&a.")

    @field:SimpleKey(node = "messages.crate.manager.reward.display.lore.sub-title")
    var crateManagerRewardDisplayLoreSubTitle = "&a${format("Write in the chat.")}"

    @field:SimpleKey(node = "messages.crate.manager.reward.display.lore.success")
    var crateManagerRewardDisplayLoreSuccess = MessageWrapper("&aLine successfully added!")

    @field:SimpleKey(node = "messages.crate.manager.reward.display.lore.cannot-empty")
    var crateManagerRewardDisplayLoreCannotEmpty = MessageWrapper("&cThe display lore cannot be empty.")

    @field:SimpleKey(node = "messages.crate.manager.reward.display.name.message")
    var crateManagerRewardDisplayNameMessage =
        MessageWrapper("&aType the new display name in the chat. To cancel, type '&ecancel&a'")

    @field:SimpleKey(node = "messages.crate.manager.reward.display.name.cannot-empty")
    var crateManagerRewardDisplayNameCannotEmpty = MessageWrapper("&cThe display name cannot be empty.")

    @field:SimpleKey(node = "messages.crate.manager.reward.display.name.already-exists")
    var crateManagerRewardDisplayNameAlreadyExists = MessageWrapper("&cThe display name already exists.")

    @field:SimpleKey(node = "messages.crate.manager.reward.display.name.success")
    var crateManagerRewardDisplayNameSuccess = MessageWrapper("&aThe display name has been updated to %name%")

    @field:SimpleKey(node = "messages.crate.manager.permission.message")
    var crateManagePermissionMessage =
        MessageWrapper("&eWrite the new permission in the chat.\n&aTo exit this session type &e\"cancel\"&a.")

    @field:SimpleKey(node = "messages.crate.manager.permission.sub-title")
    var crateManagePermissionSubTitle = "&a${format("Write in the chat.")}"

    @field:SimpleKey(node = "messages.crate.manager.permission.success")
    var crateManagePermissionSuccess = MessageWrapper("&aPermission successfully added!")

    @field:SimpleKey(node = "messages.crate.manager.permission.cannot-empty")
    var crateManagePermissionCannotEmpty = MessageWrapper("&cThe permission cannot be empty.")

    @field:SimpleKey(node = "messages.crate.manager.broadcast.message")
    var crateManageBroadcastMessage =
        MessageWrapper("&eWrite the new message in the chat.\n&aTo exit this session type &e\"cancel\"&a.")

    @field:SimpleKey(node = "messages.crate.manager.broadcast.sub-title")
    var crateManageBroadcastSubTitle = "&a${format("Write in the chat.")}"

    @field:SimpleKey(node = "messages.crate.manager.broadcast.success")
    var crateManageBroadcastSuccess = MessageWrapper("&aLine successfully added!")

    @field:SimpleKey(node = "messages.crate.manager.broadcast.cannot-empty")
    var crateManageBroadcastCannotEmpty = MessageWrapper("&cThe message cannot be empty.")

    @field:SimpleKey(node = "messages.crate.manager.item.lore.message")
    var crateManagerItemLoreMessage =
        MessageWrapper("&eWrite the new line in the chat.\n&aTo exit this session type &e\"cancel\"&a.")

    @field:SimpleKey(node = "messages.crate.manager.item.lore.sub-title")
    var crateManagerItemLoreSubTitle = "&a${format("Write in the chat.")}"

    @field:SimpleKey(node = "messages.crate.manager.item.lore.success")
    var crateManagerItemLoreSuccess = MessageWrapper("&aLine successfully added!")

    @field:SimpleKey(node = "messages.crate.manager.item.lore.cannot-empty")
    var crateManagerItemLoreCannotEmpty = MessageWrapper("&cThe item lore cannot be empty.")

    @field:SimpleKey(node = "messages.crate.manager.item.name.message")
    var crateManagerItemNameMessage =
        MessageWrapper("&aType the new item name in the chat. To cancel, type '&ecancel&a'")

    @field:SimpleKey(node = "messages.crate.manager.item.name.cannot-empty")
    var crateManagerItemNameCannotEmpty = MessageWrapper("&cThe item name cannot be empty.")

    @field:SimpleKey(node = "messages.crate.manager.item.name.already-exists")
    var crateManagerItemNameAlreadyExists = MessageWrapper("&cThe item name already exists.")

    @field:SimpleKey(node = "messages.crate.manager.item.name.success")
    var crateManagerItemNameSuccess = MessageWrapper("&aThe item name has been updated to %name%")

    @field:SimpleKey(node = "messages.crate.manager.identifier.message")
    var crateIdentifierMessage =
        MessageWrapper("&aType the new crate identifier in the chat. To cancel, type '&ecancel&a'")

    @field:SimpleKey(node = "messages.crate.manager.identifier.cannot-empty")
    var crateIdentifierCannotEmpty = MessageWrapper("&cThe Crate identifier cannot be empty.")

    @field:SimpleKey(node = "messages.crate.manager.identifier.invalid")
    var crateIdentifierInvalid = MessageWrapper("&cUse only letters, numbers, _, and -.")

    @field:SimpleKey(node = "messages.crate.manager.identifier.already-exists")
    var crateIdentifierAlreadyExists = MessageWrapper("&cA crate with the ID %id% already exists. Please try again.")

    @field:SimpleKey(node = "messages.crate.manager.identifier.success")
    var crateIdentifierSuccess = MessageWrapper("&aThe crate identifier has been changed from %old% to %new%.")

    @field:SimpleKey(node = "messages.crate.manager.delete.fail")
    var crateNotDeleted = MessageWrapper("&cThe %crate% crate could not be deleted!")

    @field:SimpleKey(node = "messages.crate.manager.delete.success")
    var crateDeleted = MessageWrapper("&eThe %crate% crate has been successfully deleted.")

    @field:SimpleKey(node = "messages.crate.manager.clone.fail")
    var crateNotCloned = MessageWrapper("&cThe %crate% crate could not be cloned!")

    @field:SimpleKey(node = "messages.crate.manager.clone.success")
    var crateCloned = MessageWrapper("&aThe %crate% crate has been successfully cloned. ID: &7%id%")

    @field:SimpleKey(node = "messages.crate.manager.attach-block.message")
    var crateAttachBlockMessage =
        MessageWrapper("&aRight-click a block to attach the crate location.")

    @field:SimpleKey(node = "messages.crate.manager.attach-block.success")
    var crateAttachBlockSuccess =
        MessageWrapper("&aCrate '&e%crate%&a' attached at &f%x% %y% %z%&a.")

    @field:SimpleKey(node = "messages.crate.open.no-key")
    var crateOpenNoKey = MessageWrapper("&cYou do not have a key to open &e%crate%&c.")

    @field:SimpleKey(node = "messages.crate.open.no-permission")
    var crateOpenNoPermission = MessageWrapper("&cYou do not have permission to open &e%crate%&c.")

    @field:SimpleKey(node = "messages.crate.open.no-rewards")
    var crateOpenNoRewards = MessageWrapper("&cThis crate has no rewards configured.")

    @field:SimpleKey(node = "messages.crate.open.disabled")
    var crateOpenDisabled = MessageWrapper("&cThis crate is currently disabled.")

    @field:SimpleKey(node = "messages.crate.open.inventory-full")
    var crateOpenInventoryFull = MessageWrapper("&cYour inventory is full. Empty space before opening this crate.")

    @field:SimpleKey(node = "messages.crate.open.reward-won")
    var crateOpenRewardWon = MessageWrapper("&aYou opened &e%crate% &aand won &e%reward%&a.")

    @field:SimpleKey(node = "messages.crate.open.already-opening")
    var crateOpenAlreadyOpening = MessageWrapper("&ePlease wait. Your previous crate opening is still running.")

    @field:SimpleKey(node = "messages.crate.open.cooldown")
    var crateOpenCooldown = MessageWrapper("&cThis crate is on cooldown. Wait &e%seconds%s&c.")
}
