package com.simplesurvival.crates.service.crate

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.crates.service.crate.objects.reward.Reward
import com.simplesurvival.crates.util.ItemNaming
import com.simplesurvival.lib.util.kyori.TextUtil
import eu.decentsoftware.holograms.api.DHAPI
import eu.decentsoftware.holograms.api.holograms.Hologram
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.scheduler.BukkitTask

object CratePreviewRewardService
{
    private const val TAG_PREFIX = "simplecrates_preview_"
    private const val HOLOGRAM_PREFIX = "simplecrates_preview_"
    private const val YAW_STEP = 8f

    private val sessions: MutableMap<String, BasePreviewSession> = mutableMapOf()
    private var task: BukkitTask? = null

    fun refreshAll()
    {
        clearAll()
        purgePersistedPreviewEntities()
        purgePreviewHolograms()
        CrateService.crates.values.forEach { refresh(it) }
        ensureTask()
    }

    fun refresh(crate: Crate)
    {
        remove(crate.identifier)

        if (!crate.options.previewReward) return

        val rewards = crate.rewards.filter { it.item.type.isItem && !it.item.type.isAir }
        if (rewards.isEmpty()) return

        crate.locations
            .mapNotNull { it.value }
            .filter { it.world != null }
            .forEachIndexed { index, location ->
                createSession(crate, rewards, location, index)
            }

        ensureTask()
    }

    fun remove(crateIdentifier: String)
    {
        val crateId = crateIdentifier.lowercase()
        val keyPrefix = "${crateId}#"
        val toRemove = sessions.keys.filter { it.startsWith(keyPrefix) }
        toRemove.forEach { key ->
            sessions.remove(key)?.dispose()
        }

        val entityPrefix = "${TAG_PREFIX}${crateId}_"
        Bukkit.getWorlds().forEach { world ->
            world.entities
                .filter { entity -> entity.scoreboardTags.any { it.startsWith(entityPrefix) } }
                .forEach { entity -> entity.remove() }
        }

        removePreviewHolograms(crateId)
    }

    fun clearAll()
    {
        sessions.values.forEach { it.dispose() }
        sessions.clear()
        purgePersistedPreviewEntities()
        purgePreviewHolograms()
    }

    private fun ensureTask()
    {
        if (task != null) return
        task = Bukkit.getScheduler().runTaskTimer(SimpleCrates.plugin, Runnable { tick() }, 1L, 2L)
    }

    private fun tick()
    {
        if (sessions.isEmpty())
        {
            task?.cancel()
            task = null
            return
        }

        val toRemove = mutableListOf<String>()
        val cratesToRefresh = mutableSetOf<String>()

        sessions.forEach { (key, session) ->
            if (!session.isValid())
            {
                session.dispose()
                toRemove.add(key)
                cratesToRefresh.add(session.crateId.lowercase())
                return@forEach
            }

            session.updateFrame()

            session.ticksUntilSwitch--
            if (session.ticksUntilSwitch <= 0)
            {
                session.rewardIndex = (session.rewardIndex + 1) % session.rewards.size
                val reward = session.rewards[session.rewardIndex]
                session.applyReward(reward)
                session.ticksUntilSwitch = session.switchTicks
            }
        }

        toRemove.forEach { sessions.remove(it) }

        cratesToRefresh.forEach { crateId ->
            val crate = CrateService.get(crateId) ?: return@forEach
            refresh(crate)
        }
    }

    private fun createSession(crate: Crate, rewards: List<Reward>, location: Location, index: Int)
    {
        val world = location.world ?: return

        val center = Location(world, location.blockX + 0.5, location.blockY.toDouble(), location.blockZ + 0.5)
        val crateId = crate.identifier.lowercase()
        val key = "${crateId}#$index"

        val initialItemHeight = crate.previewRewardItemHeight.coerceAtLeast(0.0)
        val initialTextHeight = crate.previewRewardTextHeight.coerceAtLeast(0.0)
        val firstReward = rewards.first()

        val session = if (isDecentHologramsAvailable())
        {
            createDecentHologramsSession(
                crate = crate,
                rewards = rewards,
                center = center,
                index = index,
                initialItemHeight = initialItemHeight,
                initialTextHeight = initialTextHeight,
                firstReward = firstReward
            )
        } else
        {
            createLegacyEntitySession(
                crate = crate,
                rewards = rewards,
                center = center,
                index = index,
                initialItemHeight = initialItemHeight,
                initialTextHeight = initialTextHeight,
                firstReward = firstReward
            )
        }

        session?.applyReward(firstReward)
        if (session != null)
        {
            sessions[key] = session
        }
    }

    private fun createLegacyEntitySession(
        crate: Crate,
        rewards: List<Reward>,
        center: Location,
        index: Int,
        initialItemHeight: Double,
        initialTextHeight: Double,
        firstReward: Reward
    ): BasePreviewSession?
    {
        val world = center.world ?: return null
        val entityTag = "${TAG_PREFIX}${crate.identifier.lowercase()}_$index"

        world.getNearbyEntities(center.clone().add(0.0, initialItemHeight, 0.0), 1.25, 1.25, 1.25)
            .filter { it.scoreboardTags.contains(entityTag) }
            .forEach { it.remove() }

        val item = world.spawnEntity(center.clone().add(0.0, initialItemHeight, 0.0), EntityType.ITEM) as Item
        item.itemStack = firstReward.item.cloneBuilder()
        item.setGravity(false)
        item.pickupDelay = Int.MAX_VALUE
        item.isInvulnerable = true
        item.isPersistent = false
        item.velocity = item.velocity.zero()
        item.addScoreboardTag(entityTag)

        val label = world.spawnEntity(center.clone().add(0.0, initialTextHeight, 0.0), EntityType.ARMOR_STAND) as ArmorStand
        label.isVisible = false
        label.isMarker = true
        label.isSmall = true
        label.isCustomNameVisible = true
        label.setGravity(false)
        label.isInvulnerable = true
        label.isPersistent = false
        label.addScoreboardTag(entityTag)

        val switchTicks = (crate.previewRewardSwitchSeconds.coerceAtLeast(1) * 20)
        return EntityPreviewSession(
            crateId = crate.identifier,
            center = center,
            item = item,
            label = label,
            rewards = rewards,
            rewardIndex = 0,
            switchTicks = switchTicks,
            ticksUntilSwitch = switchTicks,
            previewText = crate.previewRewardText.ifBlank { "%reward%" },
            itemHeight = initialItemHeight,
            textHeight = initialTextHeight
        )
    }

    private fun createDecentHologramsSession(
        crate: Crate,
        rewards: List<Reward>,
        center: Location,
        index: Int,
        initialItemHeight: Double,
        initialTextHeight: Double,
        firstReward: Reward
    ): BasePreviewSession?
    {
        val hologramId = previewHologramId(crate.identifier.lowercase(), index)
        DHAPI.removeHologram(hologramId)

        val minHeight = minOf(initialItemHeight, initialTextHeight)
        val baseLocation = center.clone().add(0.0, minHeight, 0.0)
        val hologram = runCatching {
            DHAPI.createHologram(hologramId, baseLocation, false, mutableListOf())
        }.getOrNull() ?: return null

        DHAPI.addHologramLine(hologram, firstReward.item.cloneBuilder())
        DHAPI.addHologramLine(hologram, "")

        val page = DHAPI.getHologramPage(hologram, 0) ?: return null
        val itemLine = page.lines.getOrNull(0)
        val textLine = page.lines.getOrNull(1)

        itemLine?.offsetY = (initialItemHeight - minHeight)
        textLine?.offsetY = (initialTextHeight - minHeight)
        itemLine?.height = 0.001
        textLine?.height = 0.001
        hologram.realignLines()
        DHAPI.updateHologram(hologramId)

        val switchTicks = (crate.previewRewardSwitchSeconds.coerceAtLeast(1) * 20)
        return DecentHologramPreviewSession(
            crateId = crate.identifier,
            hologramId = hologramId,
            rewards = rewards,
            rewardIndex = 0,
            switchTicks = switchTicks,
            ticksUntilSwitch = switchTicks,
            previewText = crate.previewRewardText.ifBlank { "%reward%" }
        )
    }

    private fun updateLabelText(previewText: String, reward: Reward): String
    {
        val textTemplate = previewText
        if (textTemplate.isBlank()) return ""

        val normalizedRewardName = ItemNaming.displayNameOrMaterial(reward.item)
        return textTemplate.replace("%reward%", normalizedRewardName).ifBlank { normalizedRewardName }
    }

    private fun purgePersistedPreviewEntities()
    {
        Bukkit.getWorlds().forEach { world ->
            world.entities
                .filter { entity -> entity.scoreboardTags.any { it.startsWith(TAG_PREFIX) } }
                .forEach { entity -> entity.remove() }
        }
    }

    private fun purgePreviewHolograms()
    {
        if (!isDecentHologramsAvailable()) return
        Hologram.getCachedHologramNames()
            .filter { it.startsWith(HOLOGRAM_PREFIX) }
            .forEach { DHAPI.removeHologram(it) }
    }

    private fun removePreviewHolograms(crateIdLower: String)
    {
        if (!isDecentHologramsAvailable()) return

        val prefix = previewHologramId(crateIdLower, 0).substringBeforeLast("_") + "_"
        Hologram.getCachedHologramNames()
            .filter { it.startsWith(prefix) }
            .forEach { DHAPI.removeHologram(it) }
    }

    private fun previewHologramId(crateIdLower: String, index: Int): String
    {
        return "${HOLOGRAM_PREFIX}${crateIdLower}_$index"
    }

    private fun isDecentHologramsAvailable(): Boolean
    {
        val plugin = Bukkit.getPluginManager().getPlugin("DecentHolograms") ?: return false
        return plugin.isEnabled
    }

    private sealed interface BasePreviewSession
    {
        val crateId: String
        val rewards: List<Reward>
        var rewardIndex: Int
        val switchTicks: Int
        var ticksUntilSwitch: Int
        val previewText: String

        fun isValid(): Boolean
        fun updateFrame()
        fun setRewardItem(reward: Reward)
        fun setRewardText(text: String)
        fun dispose()

        fun applyReward(reward: Reward)
        {
            setRewardItem(reward)
            val line = updateLabelText(previewText, reward)
            setRewardText(line)
        }
    }

    private data class EntityPreviewSession(
        override val crateId: String,
        val center: Location,
        val item: Item,
        val label: ArmorStand,
        override val rewards: List<Reward>,
        override var rewardIndex: Int,
        override val switchTicks: Int,
        override var ticksUntilSwitch: Int,
        override val previewText: String,
        val itemHeight: Double,
        val textHeight: Double,
        var yaw: Float = 0f
    ) : BasePreviewSession
    {
        override fun isValid(): Boolean
        {
            return center.world != null && item.isValid && label.isValid
        }

        override fun updateFrame()
        {
            yaw = (yaw + YAW_STEP) % 360f
            val itemLocation = center.clone().add(0.0, itemHeight, 0.0)
            itemLocation.yaw = yaw
            item.teleport(itemLocation)
            item.setRotation(yaw, 0f)

            val labelLocation = center.clone().add(0.0, textHeight, 0.0)
            label.teleport(labelLocation)
        }

        override fun setRewardItem(reward: Reward)
        {
            item.itemStack = reward.item.cloneBuilder()
        }

        override fun setRewardText(text: String)
        {
            if (text.isBlank())
            {
                label.customName(null)
                return
            }
            label.customName(TextUtil.parse(text))
        }

        override fun dispose()
        {
            if (item.isValid) item.remove()
            if (label.isValid) label.remove()
        }
    }

    private data class DecentHologramPreviewSession(
        override val crateId: String,
        val hologramId: String,
        override val rewards: List<Reward>,
        override var rewardIndex: Int,
        override val switchTicks: Int,
        override var ticksUntilSwitch: Int,
        override val previewText: String
    ) : BasePreviewSession
    {
        override fun isValid(): Boolean
        {
            return DHAPI.getHologram(hologramId) != null
        }

        override fun updateFrame()
        {
            // DecentHolograms handles rendering. We only switch rewards at interval.
        }

        override fun setRewardItem(reward: Reward)
        {
            val hologram = DHAPI.getHologram(hologramId) ?: return
            DHAPI.setHologramLine(hologram, 0, reward.item.cloneBuilder())
            DHAPI.updateHologram(hologramId)
        }

        override fun setRewardText(text: String)
        {
            val hologram = DHAPI.getHologram(hologramId) ?: return
            DHAPI.setHologramLine(hologram, 1, text)
            DHAPI.updateHologram(hologramId)
        }

        override fun dispose()
        {
            DHAPI.removeHologram(hologramId)
        }
    }
}
