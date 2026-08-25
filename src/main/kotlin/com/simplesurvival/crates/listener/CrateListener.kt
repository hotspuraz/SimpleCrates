package com.simplesurvival.crates.listener

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.crates.config.MainConfiguration
import com.simplesurvival.crates.config.MessagesConfiguration
import com.simplesurvival.crates.menu.crate.animation.CrateAnimationMenuYaml
import com.simplesurvival.crates.menu.crate.preview.CratePreviewMenu
import com.simplesurvival.crates.service.crate.Crate
import com.simplesurvival.crates.service.crate.CratePreviewRewardService
import com.simplesurvival.crates.service.crate.CrateRewardDisplayService
import com.simplesurvival.crates.service.crate.CrateService
import com.simplesurvival.crates.service.crate.objects.location.CrateLocation
import com.simplesurvival.crates.service.crate.objects.reward.Reward
import com.simplesurvival.crates.service.key.Key
import com.simplesurvival.crates.service.key.KeyService
import com.simplesurvival.crates.service.profile.CrateProfileService
import com.simplesurvival.crates.util.DisplayNameFormatUtil
import com.simplesurvival.crates.util.ItemNaming
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import com.simplesurvival.lib.util.kyori.TextUtil
import com.simplesurvival.lib.util.player.PlayerUtil
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.ChatColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.Sound
import org.bukkit.block.Lidded
import org.bukkit.block.data.Directional
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import java.util.UUID
import kotlin.math.pow
import kotlin.random.Random

class CrateListener : Listener
{

    companion object
    {
        private data class PendingRewardGrant(
            val crateId: String,
            val crateDisplayName: String,
            val rewardId: String,
            val rewardDisplayName: String,
            val winItems: List<ItemStack>,
            val winCommands: List<String>,
            val broadcastEnabled: Boolean,
            val broadcastMessages: List<String>
        )

        private data class ActiveAnimation(
            val crateId: String,
            val inventory: Inventory,
            val pendingGrant: PendingRewardGrant,
            var allowClose: Boolean = false,
            var viewDetached: Boolean = false,
            var task: BukkitTask? = null
        )

        private val attachBlockMode: MutableMap<UUID, String> = mutableMapOf()
        private val crateOpenCooldownUntil: MutableMap<UUID, MutableMap<String, Long>> = mutableMapOf()
        private val activeAnimations: MutableMap<UUID, ActiveAnimation> = mutableMapOf()
        private val pendingOfflineGrants: MutableMap<UUID, MutableList<PendingRewardGrant>> = mutableMapOf()
        private const val PREVIEW_RESTORE_DELAY_TICKS = 20L
        private const val DEBUG_REWARD_COMMANDS = false

        fun beginAttachBlock(playerId: UUID, crateIdentifier: String)
        {
            attachBlockMode[playerId] = crateIdentifier.lowercase()
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun interact(event: PlayerInteractEvent)
    {
        // RIGHT_CLICK_BLOCK usually fires for main hand and off hand.
        // Restricting to main hand prevents duplicate crate opens/key consumes.
        if (event.hand != null && event.hand != EquipmentSlot.HAND) return

        if (handleAttachBlock(event)) return
        if (handlePlaceCrateFromItem(event)) return
        handleCrateBlockInteraction(event)
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun inventoryClick(event: InventoryClickEvent)
    {
        val player = event.whoClicked as? Player ?: return
        val animation = activeAnimations[player.uniqueId] ?: return
        if (event.view.topInventory != animation.inventory) return

        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun inventoryDrag(event: InventoryDragEvent)
    {
        val player = event.whoClicked as? Player ?: return
        val animation = activeAnimations[player.uniqueId] ?: return
        if (event.view.topInventory != animation.inventory) return

        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun inventoryClose(event: InventoryCloseEvent)
    {
        val player = event.player as? Player ?: return
        val animation = activeAnimations[player.uniqueId] ?: return
        if (event.inventory != animation.inventory) return
        if (animation.allowClose) return

        // Avoid close/open loops that can spike MSPT on heavily loaded servers.
        // If the player closes the menu manually, continue the roll in detached mode
        // and grant the reward at the end as usual.
        animation.viewDetached = true
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun quit(event: PlayerQuitEvent)
    {
        val playerId = event.player.uniqueId
        crateOpenCooldownUntil.remove(playerId)
        val animation = activeAnimations.remove(playerId) ?: return

        animation.task?.cancel()
        pendingOfflineGrants.getOrPut(playerId) { mutableListOf() }.add(animation.pendingGrant)
        schedulePreviewRestoreById(animation.crateId)
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun join(event: PlayerJoinEvent)
    {
        val player = event.player
        val pending = pendingOfflineGrants.remove(player.uniqueId) ?: return

        pending.forEach { grant ->
            deliverPendingGrant(player, grant)
        }
    }

    private fun handleAttachBlock(event: PlayerInteractEvent): Boolean
    {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return false

        val player = event.player
        val crateIdentifier = attachBlockMode[player.uniqueId] ?: return false
        val clickedBlock = event.clickedBlock ?: return false

        val crate = CrateService.get(crateIdentifier)
        if (crate == null)
        {
            attachBlockMode.remove(player.uniqueId)
            return true
        }

        clickedBlock.type = crate.currentBlockMaterial()
        applyPlacementRotation(clickedBlock, player)

        crate.locations.removeAll { it.sameBlock(clickedBlock.location) }
        crate.locations.add(CrateLocation(clickedBlock.location))
        crate.update()

        attachBlockMode.remove(player.uniqueId)
        MessagesConfiguration.crateAttachBlockSuccess.send(player) {
            it.replace("%crate%", resolveCrateDisplay(crate))
                .replace("%x%", clickedBlock.x.toString())
                .replace("%y%", clickedBlock.y.toString())
                .replace("%z%", clickedBlock.z.toString())
        }

        event.isCancelled = true
        return true
    }

    private fun handlePlaceCrateFromItem(event: PlayerInteractEvent): Boolean
    {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return false

        val item = event.item ?: return false
        val clickedBlock = event.clickedBlock ?: return false

        val itemMeta = item.itemMeta ?: return false
        val container = itemMeta.persistentDataContainer

        val crateIdentifier = readCrateIdentifier(container) ?: return false
        val crate = CrateService.get(crateIdentifier) ?: return true

        val placeBlock = clickedBlock.getRelative(event.blockFace)
        if (!placeBlock.type.isAir) return true

        placeBlock.type = crate.currentBlockMaterial()
        applyPlacementRotation(placeBlock, event.player)

        crate.locations.removeAll { it.sameBlock(placeBlock.location) }
        crate.locations.add(CrateLocation(placeBlock.location))
        crate.update()

        event.isCancelled = true
        return true
    }

    private fun readCrateIdentifier(container: org.bukkit.persistence.PersistentDataContainer): String?
    {
        val pluginKey = NamespacedKey(SimpleCrates.plugin, "crate-id")
        if (container.has(pluginKey, PersistentDataType.STRING))
        {
            return container.get(pluginKey, PersistentDataType.STRING)
        }

        val legacyKey = NamespacedKey.fromString("crate-id")
        if (legacyKey != null && container.has(legacyKey, PersistentDataType.STRING))
        {
            return container.get(legacyKey, PersistentDataType.STRING)
        }

        return null
    }

    private fun handleCrateBlockInteraction(event: PlayerInteractEvent)
    {
        val action = event.action
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) return

        val clickedBlock = event.clickedBlock ?: return
        val crate = crateByBlock(clickedBlock.location) ?: return

        event.isCancelled = true

        if (action == Action.LEFT_CLICK_BLOCK && event.player.isSneaking)
        {
            if (tryBreakCrateAsAdmin(event.player, crate, clickedBlock.location))
            {
                return
            }
        }

        if (action == Action.RIGHT_CLICK_BLOCK)
        {
            openCrate(event.player, crate, clickedBlock.location)
            return
        }

        openPreview(event.player, crate)
    }

    private fun crateByBlock(blockLocation: org.bukkit.Location): Crate?
    {
        return CrateService.crates.values.firstOrNull { crate ->
            crate.locations.any { it.sameBlock(blockLocation) }
        }
    }

    private fun openCrate(player: Player, crate: Crate, blockLocation: org.bukkit.Location)
    {
        val crateDisplayForMessage = resolveCrateDisplay(crate)
        val activeAnimation = activeAnimations[player.uniqueId]
        if (activeAnimation != null && !activeAnimation.allowClose)
        {
            MessagesConfiguration.crateOpenAlreadyOpening.send(player)
            return
        }

        val crateId = crate.identifier.lowercase()
        val now = System.currentTimeMillis()
        val cooldownUntil = crateOpenCooldownUntil[player.uniqueId]?.get(crateId) ?: 0L
        if (now < cooldownUntil)
        {
            val remainingSeconds = (cooldownUntil - now).coerceAtLeast(0L).toDouble() / 1000.0
            MessagesConfiguration.crateOpenCooldown.send(player) {
                it.replace("%seconds%", String.format("%.1f", remainingSeconds))
            }
            return
        }

        if (!crate.options.enabled)
        {
            MessagesConfiguration.crateOpenDisabled.send(player) {
                it.replace("%crate%", crateDisplayForMessage)
            }
            return
        }

        if (crate.permission.required && crate.permission.key.isNotBlank() && !player.hasPermission(crate.permission.key))
        {
            MessagesConfiguration.crateOpenNoPermission.send(player) {
                it.replace("%crate%", crateDisplayForMessage)
            }
            return
        }

        val eligibleRewards = eligibleRewards(crate)
        if (eligibleRewards.isEmpty())
        {
            MessagesConfiguration.crateOpenNoRewards.send(player) {
                it.replace("%crate%", crateDisplayForMessage)
            }
            return
        }

        if (!canFitAllRewardOutcomes(player, eligibleRewards))
        {
            MessagesConfiguration.crateOpenInventoryFull.send(player)
            return
        }

        if (crate.key.required && !consumeRequiredKey(player, crate))
        {
            applyNoKeyFeedback(player)
            MessagesConfiguration.crateOpenNoKey.send(player) {
                it.replace("%crate%", crateDisplayForMessage)
            }
            return
        }

        val reward = rollReward(eligibleRewards) ?: run {
            MessagesConfiguration.crateOpenNoRewards.send(player) {
                it.replace("%crate%", crateDisplayForMessage)
            }
            return
        }
        if (cooldownUntil > 0L)
        {
            crateOpenCooldownUntil[player.uniqueId]?.remove(crateId)
        }

        val configuredCooldownSeconds = crate.options.openCooldownInSeconds.coerceAtLeast(0)
        if (configuredCooldownSeconds > 0)
        {
            val cooldownUntilMillis = now + (configuredCooldownSeconds * 1000L)
            crateOpenCooldownUntil
                .getOrPut(player.uniqueId) { mutableMapOf() }[crateId] = cooldownUntilMillis
        } else
        {
            crateOpenCooldownUntil[player.uniqueId]?.remove(crateId)
        }

        CratePreviewRewardService.remove(crate.identifier)

        val rewardVisualItem = animationDisplayItem(reward)
        val rewardItem = rewardVisualItem.cloneBuilder()
        val wonRewardName = ItemNaming.displayNameOrMaterial(rewardVisualItem)

        val pendingGrant = buildPendingGrant(crate, reward)
        val finishReward: () -> Unit = {
            if (isChestCrate(crate))
            {
                showChestRewardDisplay(crate, blockLocation, rewardItem, wonRewardName)
            }

            deliverPendingGrant(player, pendingGrant)

            MessagesConfiguration.crateOpenRewardWon.send(player) {
                it.replace("%crate%", crateDisplayForMessage)
                    .replace("%reward%", wonRewardName)
            }

            schedulePreviewRestore(crate)
        }

        if (crate.animationEnabled)
        {
            CratePreviewRewardService.remove(crate.identifier)
            playCrateOpenAnimation(player, crate, reward, pendingGrant, finishReward)
        } else
        {
            CratePreviewRewardService.remove(crate.identifier)
            finishReward()
        }
    }

    private fun openPreview(player: Player, crate: Crate)
    {
        CratePreviewMenu(player, crate).build()
    }

    private fun applyNoKeyFeedback(player: Player)
    {
        if (!MainConfiguration.crateOpenNoKeyPushbackEnabled) return

        val horizontal = MainConfiguration.crateOpenNoKeyPushbackHorizontalStrength.coerceAtLeast(0.0)
        val vertical = MainConfiguration.crateOpenNoKeyPushbackVerticalStrength
        val direction = player.location.direction

        val push = Vector(-direction.x, 0.0, -direction.z)
        if (push.lengthSquared() > 0.0)
        {
            push.normalize().multiply(horizontal)
        }
        push.y = vertical
        player.velocity = push

        playConfiguredSound(
            player,
            MainConfiguration.crateOpenNoKeyPushbackSound,
            MainConfiguration.crateOpenNoKeyPushbackSoundVolume,
            MainConfiguration.crateOpenNoKeyPushbackSoundPitch
        )
    }

    private fun consumeRequiredKey(player: Player, crate: Crate): Boolean
    {
        if (crate.key.ids.isEmpty()) return false

        val linkedKeys = crate.key.ids.mapNotNull { KeyService.get(it) }
            .filter { it.enabled }

        if (linkedKeys.isEmpty()) return false

        for (key in linkedKeys)
        {
            if (key.virtual)
            {
                val profile = CrateProfileService.instance.load(player.uniqueId)
                if (profile.getKey(key.identifier) > 0)
                {
                    profile.removeKey(key.identifier, 1)
                    return true
                }
                continue
            }

            if (removePhysicalKey(player, key))
            {
                return true
            }
        }

        return false
    }

    private fun removePhysicalKey(player: Player, key: Key): Boolean
    {
        val contents = player.inventory.contents

        for (index in contents.indices)
        {
            val stack = contents[index] ?: continue
            if (!stack.isSimilar(key.item)) continue

            if (stack.amount > 1)
            {
                stack.amount -= 1
                player.inventory.setItem(index, stack)
            } else
            {
                player.inventory.setItem(index, null)
            }

            player.updateInventory()
            return true
        }

        return false
    }

    private fun canFitRewardItems(player: Player, winItems: List<ItemStack>): Boolean
    {
        if (winItems.isEmpty()) return true

        val simulated = Bukkit.createInventory(null, 36)
        player.inventory.storageContents.forEachIndexed { index, stack ->
            if (stack != null)
            {
                simulated.setItem(index, stack.clone())
            }
        }

        winItems.forEach { item ->
            val leftovers = simulated.addItem(item.clone())
            if (leftovers.isNotEmpty())
            {
                return false
            }
        }

        return true
    }

    private fun canFitAllRewardOutcomes(player: Player, rewards: List<Reward>): Boolean
    {
        return rewards.all { reward -> canFitRewardItems(player, reward.winItems) }
    }

    private fun grantRewardItems(player: Player, winItems: List<ItemStack>): Boolean
    {
        if (!canFitRewardItems(player, winItems))
        {
            return false
        }

        winItems.forEach { item ->
            player.inventory.addItem(item.clone())
        }

        return true
    }

    private fun eligibleRewards(crate: Crate): List<Reward>
    {
        return crate.rewards.filter { reward ->
            reward.weight > 0 && (reward.winCommands.isNotEmpty() || reward.winItems.isNotEmpty())
        }
    }

    private fun rollReward(eligibleRewards: List<Reward>): Reward?
    {
        if (eligibleRewards.isEmpty()) return null

        val totalWeight = eligibleRewards.sumOf { it.weight }
        if (totalWeight <= 0.0) return null

        var roll = Random.nextDouble(totalWeight)
        for (reward in eligibleRewards)
        {
            roll -= reward.weight
            if (roll < 0.0)
            {
                return reward
            }
        }

        return eligibleRewards.lastOrNull()
    }

    private fun deliverReward(player: Player, crate: Crate, reward: Reward)
    {
        val granted = grantRewardItems(player, reward.winItems.map { it.cloneBuilder() })
        if (!granted)
        {
            MessagesConfiguration.crateOpenInventoryFull.send(player)
            return
        }

        reward.winCommands.forEach { rawCommand ->
            val formattedCommand = rawCommand
                .replace("%player%", player.name)
                .replace("%player_name%", player.name)
                .replace("%crate%", resolveCrateDisplay(crate))
                .replace("%crate_id%", crate.identifier)
                .replace("%reward%", reward.identifier)

            executeRewardCommand(player, formattedCommand)
        }
    }

    private fun broadcastReward(player: Player, crate: Crate, reward: Reward)
    {
        if (!reward.broadcastMessageEnabled) return
        if (crate.broadcastMessages.isEmpty()) return

        val crateDisplay = resolveCrateDisplay(crate)
        val rewardDisplay = ItemNaming.displayNameOrMaterial(reward.item)

        crate.broadcastMessages.forEach { rawLine ->
            val replaced = rawLine
                .replace("%player%", player.name)
                .replace("%crate%", crateDisplay)
                .replace("%crate_id%", crate.identifier)
                .replace("%reward%", rewardDisplay)

            val parsed = applyPlaceholderApi(player, replaced)
            Bukkit.broadcast(TextUtil.parse(parsed))
        }
    }

    private fun applyPlaceholderApi(player: Player, line: String): String
    {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) return line

        return runCatching {
            PlaceholderAPI.setPlaceholders(player, line)
        }.getOrDefault(line)
    }

    private fun executeRewardCommand(player: Player, rawCommand: String)
    {
        val baseCommand = rawCommand.trim()
        val isCmiCommand = baseCommand.startsWith("cmi ", ignoreCase = true)
        val commandWithPlaceholders = if (isCmiCommand)
        {
            // Keep CMI command formatting intact (hex/gradient tags, lore/name tokens)
            // to match behavior of manually typed /cmi commands.
            baseCommand
        } else
        {
            applyPlaceholderApi(player, baseCommand).trim()
        }
        if (commandWithPlaceholders.isBlank()) return

        val executeAsPlayer = commandWithPlaceholders.startsWith("*")
        val commandBody = if (executeAsPlayer)
        {
            commandWithPlaceholders.removePrefix("*").trim()
        } else
        {
            commandWithPlaceholders
        }

        val command = commandBody.removePrefix("/").trim()
        if (command.isBlank()) return

        val runAsPlayer = executeAsPlayer
        val senderType = if (runAsPlayer) "PLAYER" else "CONSOLE"

        if (DEBUG_REWARD_COMMANDS)
        {
            SimpleCrates.plugin.logger.info(
                "[SimpleCrates][RewardCommand] player='${player.name}' " +
                        "isCmi=$isCmiCommand explicitPlayer=$executeAsPlayer sender=$senderType " +
                        "raw='$rawCommand' prepared='$commandWithPlaceholders' final='$command'"
            )
        }

        runCatching {
            val result = if (runAsPlayer)
            {
                player.performCommand(command)
            } else
            {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
            }
            if (DEBUG_REWARD_COMMANDS)
            {
                SimpleCrates.plugin.logger.info(
                    "[SimpleCrates][RewardCommand] player='${player.name}' sender=$senderType result=$result command='$command'"
                )
            }
        }.onFailure { ex ->
            SimpleCrates.plugin.logger.warning("Failed to execute reward command '$command': ${ex.message}")
            if (DEBUG_REWARD_COMMANDS)
            {
                SimpleCrates.plugin.logger.warning(
                    "[SimpleCrates][RewardCommand] failure player='${player.name}' sender=$senderType raw='$rawCommand'"
                )
            }
        }
    }

    private fun playCrateOpenAnimation(
        player: Player,
        crate: Crate,
        wonReward: Reward,
        pendingGrant: PendingRewardGrant,
        onComplete: () -> Unit
    )
    {
        val rows = CrateAnimationMenuYaml.rows.coerceIn(1, 6)
        val title = ChatColor.translateAlternateColorCodes(
            '&',
            CrateAnimationMenuYaml.title.replace("%crate%", resolveCrateDisplay(crate))
        )
        val inventory = Bukkit.createInventory(null, rows * 9, title)
        val framePeriod = CrateAnimationMenuYaml.framePeriodTicks.coerceAtLeast(1L)
        val dramaticPauseTicks = CrateAnimationMenuYaml.dramaticPauseTicks.coerceAtLeast(0L)
        val finalHoldTicks = maxOf(dramaticPauseTicks, 10L)
        val rewards =
            crate.rewards.filter { it.weight > 0 && (it.winCommands.isNotEmpty() || it.winItems.isNotEmpty()) }
        val totalWeight = rewards.sumOf { it.weight }.coerceAtLeast(0.000001)
        val middleSlots = CrateAnimationMenuYaml.middleSlots.filter { it in 0 until inventory.size }
        val centerIndex = middleSlots.size / 2
        val rollDelays = buildRouletteDelays(framePeriod, CrateAnimationMenuYaml.durationTicks)
        val spinFrames = rollDelays.size.coerceAtLeast(1)
        val guaranteedTail = buildGuaranteedWinningTail(rewards, wonReward, middleSlots.size)
        val reel = buildReelSequence(
            rewards = rewards,
            wonReward = wonReward,
            totalFrames = spinFrames,
            visibleCount = middleSlots.size,
            guaranteedTail = guaranteedTail
        )

        if (rewards.isEmpty() || middleSlots.isEmpty())
        {
            onComplete()
            return
        }

        val palette = CrateAnimationMenuYaml.glassMaterials
            .mapNotNull { materialName -> runCatching { Material.valueOf(materialName.uppercase()) }.getOrNull() }
            .filter { it.isItem && !it.isAir }
            .ifEmpty { listOf(Material.LIGHT_BLUE_STAINED_GLASS_PANE, Material.CYAN_STAINED_GLASS_PANE) }

        val fillerStack = CrateAnimationMenuYaml.filler.cloneBuilder()
        val topSlots = (0..8).filter { it < inventory.size }
        val bottomStart = inventory.size - 9
        val bottomSlots = (bottomStart until inventory.size).filter { it in 0 until inventory.size }

        val animation = ActiveAnimation(crate.identifier, inventory, pendingGrant)
        val previousAnimation = activeAnimations[player.uniqueId]
        if (previousAnimation != null && !previousAnimation.allowClose)
        {
            // Safety net: avoid replacing an active roll and losing the previous reward.
            return
        }

        player.openInventory(inventory)
        activeAnimations[player.uniqueId] = animation
        playConfiguredSound(
            player,
            CrateAnimationMenuYaml.openSound,
            CrateAnimationMenuYaml.openVolume,
            CrateAnimationMenuYaml.openPitch
        )
        playConfiguredSound(player, "block.note_block.bit", 0.65, 1.45)
        playConfiguredSound(player, "block.amethyst_block.resonate", 0.35, 1.0)

        var frame = 0

        fun finishAnimation()
        {
            val landedFrame = (frame - 1).coerceAtLeast(0)
            val landed = reel[(landedFrame + centerIndex).coerceIn(0, reel.lastIndex)]
            if (landed.id != wonReward.id)
            {
                SimpleCrates.plugin.logger.warning(
                    "Animation desync prevented for crate '${crate.identifier}': " +
                            "landed='${landed.identifier}', won='${wonReward.identifier}'."
                )
            }
            if (!player.isOnline)
            {
                val active = activeAnimations.remove(player.uniqueId)
                if (active != null)
                {
                    pendingOfflineGrants.getOrPut(player.uniqueId) { mutableListOf() }.add(active.pendingGrant)
                    schedulePreviewRestoreById(active.crateId)
                }
                return
            }

            val centerSlot = middleSlots.getOrNull(centerIndex)
            middleSlots.forEachIndexed { index, slot ->
                val reward = reel[(landedFrame + index).coerceIn(0, reel.lastIndex)]
                val chance = (reward.weight / totalWeight) * 100.0
                inventory.setItem(slot, buildAnimatedRewardItem(reward, chance))
            }
            if (centerSlot != null)
            {
                val wonChance = (wonReward.weight / totalWeight) * 100.0
                inventory.setItem(centerSlot, buildWinningAnimatedRewardItem(wonReward, wonChance))
            }

            playConfiguredSound(
                player,
                CrateAnimationMenuYaml.finishSound,
                CrateAnimationMenuYaml.finishVolume,
                CrateAnimationMenuYaml.finishPitch
            )
            Bukkit.getScheduler().runTaskLater(SimpleCrates.plugin, Runnable {
                val active = activeAnimations[player.uniqueId]
                if (active != null && active.inventory == inventory)
                {
                    active.allowClose = true
                }
                if (player.isOnline && player.openInventory.topInventory == inventory)
                {
                    player.closeInventory()
                }
                activeAnimations.remove(player.uniqueId)
                onComplete()
            }, finalHoldTicks)
        }

        fun scheduleFrame(frameIndex: Int)
        {
            val delay = rollDelays.getOrNull(frameIndex) ?: run {
                finishAnimation()
                return
            }

            val task = Bukkit.getScheduler().runTaskLater(SimpleCrates.plugin, Runnable {
                if (!player.isOnline)
                {
                    val active = activeAnimations.remove(player.uniqueId)
                    if (active != null)
                    {
                        pendingOfflineGrants.getOrPut(player.uniqueId) { mutableListOf() }.add(active.pendingGrant)
                        schedulePreviewRestoreById(active.crateId)
                    }
                    return@Runnable
                }

                val active = activeAnimations[player.uniqueId]
                if (active == null || active.inventory != inventory)
                {
                    return@Runnable
                }

                if (!active.viewDetached)
                {
                    topSlots.forEachIndexed { index, slot ->
                        val material = palette[(frame + index) % palette.size]
                        inventory.setItem(slot, ItemStack(material))
                    }
                    bottomSlots.forEachIndexed { index, slot ->
                        val material = palette[(frame + index) % palette.size]
                        inventory.setItem(slot, ItemStack(material))
                    }

                    middleSlots.forEachIndexed { index, slot ->
                        val reward = reel[frame + index]
                        val chance = (reward.weight / totalWeight) * 100.0
                        inventory.setItem(slot, buildAnimatedRewardItem(reward, chance))
                    }

                    val progress = ((frameIndex + 1).toDouble() / spinFrames.toDouble()).coerceIn(0.0, 1.0)
                    val pitchBoost = when
                    {
                        progress < 0.65 -> 0.12
                        progress < 0.9 -> 0.05
                        else -> -0.03
                    }
                    val tickPitch = (CrateAnimationMenuYaml.tickPitch + pitchBoost).coerceIn(0.5, 2.0)
                    playConfiguredSound(
                        player,
                        CrateAnimationMenuYaml.tickSound,
                        CrateAnimationMenuYaml.tickVolume,
                        tickPitch
                    )
                    playConfiguredSound(
                        player,
                        "block.note_block.hat",
                        0.18,
                        (1.65 - progress * 0.6).coerceIn(0.8, 2.0)
                    )
                }

                frame++
                scheduleFrame(frameIndex + 1)
            }, delay)
            animation.task = task
        }

        scheduleFrame(0)
    }

    private fun buildRouletteDelays(baseFramePeriod: Long, targetDurationTicks: Int): List<Long>
    {
        val fastDelay = baseFramePeriod.coerceAtLeast(1L)
        val targetTicks = targetDurationTicks.coerceAtLeast(12).toLong()
        val frameCount = (targetTicks / fastDelay).coerceAtLeast(18L).toInt()
        val baseTotalTicks = frameCount * fastDelay
        val desiredTotalTicks = maxOf(baseTotalTicks, targetTicks + (fastDelay * 20L))
        val extraTicksBudget = (desiredTotalTicks - baseTotalTicks).coerceAtLeast(0L)

        if (frameCount <= 0)
        {
            return listOf(fastDelay)
        }

        val tailWeights = List(frameCount) { index ->
            if (frameCount == 1)
            {
                1.0
            } else
            {
                (index.toDouble() / (frameCount - 1).toDouble()).pow(2.35)
            }
        }
        val weightSum = tailWeights.sum().takeIf { it > 0.0 } ?: 1.0
        val delays = MutableList(frameCount) { fastDelay }
        var allocatedExtra = 0L
        var previous = fastDelay

        for (index in 0 until frameCount)
        {
            val proportionalExtra = (extraTicksBudget.toDouble() * (tailWeights[index] / weightSum)).toLong()
            val delay = (fastDelay + proportionalExtra).coerceAtLeast(previous)
            delays[index] = delay
            previous = delay
            allocatedExtra += (delay - fastDelay)
        }

        var remainder = (extraTicksBudget - allocatedExtra).coerceAtLeast(0L)
        var index = delays.lastIndex
        while (remainder > 0L && index >= 0)
        {
            delays[index] = delays[index] + 1L
            remainder--
            index--
            if (index < 0 && remainder > 0L)
            {
                index = delays.lastIndex
            }
        }

        return delays
    }

    private fun buildReelSequence(
        rewards: List<Reward>,
        wonReward: Reward,
        totalFrames: Int,
        visibleCount: Int,
        guaranteedTail: List<Reward>
    ): List<Reward>
    {
        // `totalFrames` is the number of rendered frames.
        // The final rendered frame uses start index (totalFrames - 1), so the guaranteed
        // winner tail must start exactly there to place `wonReward` at center on the last frame.
        val randomLength = (totalFrames - 1 + visibleCount - guaranteedTail.size).coerceAtLeast(0)
        val result = ArrayList<Reward>(randomLength + guaranteedTail.size)
        repeat(randomLength)
        {
            result.add(pickWeightedReward(rewards))
        }
        result.addAll(guaranteedTail)
        if (result.isEmpty())
        {
            result.add(wonReward)
        }
        return result
    }

    private fun buildGuaranteedWinningTail(rewards: List<Reward>, wonReward: Reward, visibleCount: Int): List<Reward>
    {
        if (visibleCount <= 0) return listOf(wonReward)

        val centerIndex = visibleCount / 2
        return List(visibleCount) { index ->
            if (index == centerIndex) wonReward else pickWeightedReward(rewards)
        }
    }

    private fun pickWeightedReward(rewards: List<Reward>): Reward
    {
        if (rewards.isEmpty()) throw IllegalArgumentException("rewards cannot be empty")
        val totalWeight = rewards.sumOf { it.weight }.coerceAtLeast(0.000001)
        var roll = Random.nextDouble(totalWeight)
        for (reward in rewards)
        {
            roll -= reward.weight
            if (roll < 0.0) return reward
        }
        return rewards.last()
    }

    private fun buildAnimatedRewardItem(reward: Reward, chance: Double): ItemStack
    {
        val itemBuilder = animationDisplayItem(reward).cloneBuilder()
        val chanceText = String.format("%.2f", chance.coerceAtLeast(0.0))
        val lore = CrateAnimationMenuYaml.rewardLore.map {
            it.replace("%chance%", chanceText)
        }
        itemBuilder.withLore(lore)
        return itemBuilder
    }

    private fun buildWinningAnimatedRewardItem(reward: Reward, chance: Double): ItemStack
    {
        val itemBuilder = buildAnimatedRewardItem(reward, chance) as? SimpleItemBuilder
            ?: return buildAnimatedRewardItem(reward, chance)

        itemBuilder.withGlow(true)
        return itemBuilder
    }

    private fun animationDisplayItem(reward: Reward): SimpleItemBuilder
    {
        val source = reward.winItems.firstOrNull()?.cloneBuilder() ?: reward.item.cloneBuilder()
        return normalizeItemName(source)
    }

    private fun playConfiguredSound(player: Player, rawSound: String, volume: Double, pitch: Double)
    {
        val normalized = normalizeSoundKey(rawSound) ?: return

        val key =
            (if (normalized.contains(':')) NamespacedKey.fromString(normalized) else NamespacedKey.minecraft(normalized))
                ?: return

        val sound = Registry.SOUNDS.get(key) ?: return
        player.playSound(player.location, sound, volume.toFloat(), pitch.toFloat())
    }

    private fun normalizeSoundKey(rawSound: String): String?
    {
        val clean = rawSound.trim()
        if (clean.isBlank()) return null
        if (clean.contains(':')) return clean.lowercase()

        val lower = clean.lowercase()
        if (lower.contains('.')) return lower

        // Supports enum-like names such as BLOCK_ENDER_CHEST_OPEN from YAML.
        return lower.replace('_', '.')
    }

    private fun schedulePreviewRestore(crate: Crate)
    {
        Bukkit.getScheduler().runTaskLater(SimpleCrates.plugin, Runnable {
            val currentCrate = CrateService.get(crate.identifier) ?: return@Runnable
            CratePreviewRewardService.refresh(currentCrate)
        }, PREVIEW_RESTORE_DELAY_TICKS)
    }

    private fun schedulePreviewRestoreById(crateId: String)
    {
        Bukkit.getScheduler().runTaskLater(SimpleCrates.plugin, Runnable {
            val currentCrate = CrateService.get(crateId) ?: return@Runnable
            CratePreviewRewardService.refresh(currentCrate)
        }, PREVIEW_RESTORE_DELAY_TICKS)
    }

    private fun buildPendingGrant(crate: Crate, reward: Reward): PendingRewardGrant
    {
        return PendingRewardGrant(
            crateId = crate.identifier,
            crateDisplayName = resolveCrateDisplay(crate),
            rewardId = reward.identifier,
            rewardDisplayName = ItemNaming.displayNameOrMaterial(reward.item),
            winItems = reward.winItems.map { normalizeItemName(it.cloneBuilder()) },
            winCommands = reward.winCommands.toList(),
            broadcastEnabled = reward.broadcastMessageEnabled,
            broadcastMessages = crate.broadcastMessages.toList()
        )
    }

    private fun resolveCrateDisplay(crate: Crate): String
    {
        return DisplayNameFormatUtil.normalize(crate.item.rawDisplayName.ifBlank { crate.identifier })
    }

    private fun deliverPendingGrant(player: Player, grant: PendingRewardGrant)
    {
        val granted = grantRewardItems(player, grant.winItems.map { normalizeGrantedItem(it) })
        if (!granted)
        {
            pendingOfflineGrants.getOrPut(player.uniqueId) { mutableListOf() }.add(grant)
            MessagesConfiguration.crateOpenInventoryFull.send(player)
            return
        }

        grant.winCommands.forEach { rawCommand ->
            val formattedCommand = rawCommand
                .replace("%player%", player.name)
                .replace("%player_name%", player.name)
                .replace("%crate%", grant.crateDisplayName)
                .replace("%crate_id%", grant.crateId)
                .replace("%reward%", grant.rewardId)

            executeRewardCommand(player, formattedCommand)
        }

        if (grant.broadcastEnabled && grant.broadcastMessages.isNotEmpty())
        {
            grant.broadcastMessages.forEach { rawLine ->
                val replaced = rawLine
                    .replace("%player%", player.name)
                    .replace("%crate%", grant.crateDisplayName)
                    .replace("%crate_id%", grant.crateId)
                    .replace("%reward%", grant.rewardDisplayName)

                val parsed = applyPlaceholderApi(player, replaced)
                Bukkit.broadcast(TextUtil.parse(parsed))
            }
        }
    }

    private fun tryBreakCrateAsAdmin(player: Player, crate: Crate, blockLocation: org.bukkit.Location): Boolean
    {
        val hasBreakPermission = player.isOp || player.hasPermission("simplecrates.admin")
        if (!hasBreakPermission) return false

        val block = blockLocation.block
        block.type = Material.AIR

        crate.locations.removeAll { it.sameBlock(blockLocation) }
        crate.update()

        val crateItem = buildCratePlacementItem(crate)
        if (PlayerUtil.isInventoryFull(player))
        {
            player.world.dropItemNaturally(player.location, crateItem)
        } else
        {
            player.inventory.addItem(crateItem)
            player.updateInventory()
        }

        return true
    }

    private fun buildCratePlacementItem(crate: Crate): ItemStack
    {
        val itemBuilder = crate.item.cloneBuilder()
        itemBuilder.type = crate.currentBlockMaterial()
        itemBuilder.withPersistentDatas(
            SimpleItemBuilder.SimplePersistentData(
                NamespacedKey(SimpleCrates.plugin, "crate-id"),
                PersistentDataType.STRING,
                crate.identifier
            )
        )
        return itemBuilder
    }

    private fun isChestCrate(crate: Crate): Boolean
    {
        return crate.item.type == Material.CHEST ||
                crate.item.type == Material.TRAPPED_CHEST ||
                crate.item.type == Material.ENDER_CHEST
    }

    private fun applyPlacementRotation(block: org.bukkit.block.Block, player: Player)
    {
        val blockData = block.blockData
        val directional = blockData as? Directional ?: return

        val preferredFacing = player.facing.oppositeFace
        directional.facing = if (directional.faces.contains(preferredFacing))
        {
            preferredFacing
        } else
        {
            directional.faces.firstOrNull() ?: directional.facing
        }

        block.blockData = directional
    }

    private fun showChestRewardDisplay(
        crate: Crate,
        blockLocation: org.bukkit.Location,
        rewardItem: org.bukkit.inventory.ItemStack,
        rewardText: String
    )
    {
        val world = blockLocation.world ?: return
        val displayLocation = blockLocation.clone().add(0.5, crate.previewRewardItemHeight, 0.5)
        val liddedState = blockLocation.block.state as? Lidded
        val isEnderChest = blockLocation.block.type == Material.ENDER_CHEST

        if (liddedState != null)
        {
            liddedState.open()
        } else
        {
            world.playSound(
                blockLocation,
                if (isEnderChest) Sound.BLOCK_ENDER_CHEST_OPEN else Sound.BLOCK_CHEST_OPEN,
                1.0f,
                1.0f
            )
        }

        val displayTag = CrateRewardDisplayService.buildTag(crate.identifier, blockLocation)
        CrateRewardDisplayService.remove(displayTag)

        val dropped = world.dropItem(displayLocation, rewardItem)
        dropped.setGravity(false)
        dropped.isInvulnerable = true
        dropped.isPersistent = false
        dropped.pickupDelay = Int.MAX_VALUE
        dropped.velocity = dropped.velocity.zero()
        dropped.addScoreboardTag(displayTag)

        val textOffset = crate.previewRewardTextHeight - crate.previewRewardItemHeight
        val label =
            world.spawnEntity(displayLocation.clone().add(0.0, textOffset, 0.0), EntityType.ARMOR_STAND) as ArmorStand
        label.isVisible = false
        label.isMarker = true
        label.isSmall = true
        label.isCustomNameVisible = true
        label.setGravity(false)
        label.isInvulnerable = true
        label.isPersistent = false
        label.customName(TextUtil.parse(rewardText))
        label.addScoreboardTag(displayTag)

        Bukkit.getScheduler().runTaskLater(SimpleCrates.plugin, Runnable {
            CrateRewardDisplayService.remove(displayTag)

            if (liddedState != null)
            {
                liddedState.close()
            } else
            {
                world.playSound(
                    blockLocation,
                    if (isEnderChest) Sound.BLOCK_ENDER_CHEST_CLOSE else Sound.BLOCK_CHEST_CLOSE,
                    1.0f,
                    1.0f
                )
            }
        }, 40L)
    }

    private fun normalizeGrantedItem(source: ItemStack): ItemStack
    {
        val builder = if (source is SimpleItemBuilder) source.cloneBuilder() else SimpleItemBuilder.clone(source)
        return normalizeItemName(builder)
    }

    private fun normalizeItemName(item: SimpleItemBuilder): SimpleItemBuilder
    {
        val normalized = DisplayNameFormatUtil.normalize(item.rawDisplayName).trim()
        val normalizedLore = item.rawLore.map { DisplayNameFormatUtil.normalize(it) }
        if (normalized.isNotBlank())
        {
            item.withName(normalized)
        }
        if (normalizedLore.isNotEmpty())
        {
            item.withLore(normalizedLore)
        }
        return item
    }
}
