package com.simplesurvival.crates.service.crate

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.crates.service.crate.objects.engine.CrateEngine
import com.simplesurvival.crates.service.crate.objects.engine.CrateEngineType
import com.simplesurvival.crates.service.crate.objects.engine.types.VanillaBlock
import com.simplesurvival.crates.service.crate.objects.engine.types.VanillaModel
import com.simplesurvival.crates.service.crate.objects.hologram.CrateHologram
import com.simplesurvival.crates.service.crate.objects.key.CrateKey
import com.simplesurvival.crates.service.crate.objects.location.CrateLocation
import com.simplesurvival.crates.service.crate.objects.option.CrateOptions
import com.simplesurvival.crates.service.crate.objects.permission.CratePermission
import com.simplesurvival.crates.service.crate.objects.reward.Reward
import com.simplesurvival.lib.configuration.serializer.ConfigSerializerRegistry
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.*

object CrateService
{

    val crates: MutableMap<String, Crate> = mutableMapOf()

    fun get(identifier: String): Crate? = crates[identifier.lowercase()]

    fun ensureBlocksPlaced(): Int
    {
        var restored = 0

        crates.values.forEach { crate ->
            val targetMaterial = crate.currentBlockMaterial()
            crate.locations
                .mapNotNull { it.value }
                .filter { it.world != null }
                .forEach { location ->
                    val block = location.block
                    if (block.type != targetMaterial)
                    {
                        block.type = targetMaterial
                        restored++
                    }
                }
        }

        return restored
    }

    fun updatePlacedBlocks(crate: Crate): Int
    {
        var updated = 0
        val targetMaterial = crate.currentBlockMaterial()

        crate.locations
            .mapNotNull { it.value }
            .filter { it.world != null }
            .forEach { location ->
                val block = location.block
                if (block.type != targetMaterial)
                {
                    block.type = targetMaterial
                    updated++
                }
            }

        return updated
    }

    fun init(plugin: JavaPlugin)
    {

        var loaded = 0

        plugin.logger.info { "Loading crates..." }

        crates.clear()

        val cratesFolder = File(plugin.dataFolder, "crates")

        if (!cratesFolder.exists())
        {
            cratesFolder.mkdirs()
        }

        // Pegando todos os arquivos .yml da pasta de crates
        cratesFolder.listFiles()
            .filter { !it.isDirectory && it.name.endsWith(".yml") }
            .forEach { file ->
                val crate = loadCrate(file)

                crates[crate.identifier.lowercase()] = crate
                loaded++
            }

        plugin.logger.info { "Loaded $loaded crates." }
    }

    fun renameCrate(oldIdentifier: String, newIdentifier: String): Boolean
    {
        val oldId = oldIdentifier.lowercase()
        val newId = newIdentifier.lowercase()

        if (oldId == newId) return true

        val crate = crates[oldId] ?: return false

        if (crates.containsKey(newId)) return false

        val cratesFolder = File(SimpleCrates.plugin.dataFolder, "crates")

        val oldFile = File(cratesFolder, "$oldId.yml")
        val newFile = File(cratesFolder, "$newId.yml")

        if (newFile.exists()) return false

        return runCatching {
            CrateHologramService.remove(crate)
            crates.remove(oldId)

            crate.identifier = newId

            updateCrate(crate)

            if (oldFile.exists())
            {
                oldFile.delete()
            }

            crates[newId] = crate
        }.isSuccess
    }

    fun deleteCrate(identifier: String): Boolean
    {
        val id = identifier.lowercase()
        val crate = crates[id] ?: return false

        val file = File(SimpleCrates.plugin.dataFolder, "crates/${id}.yml")

        return runCatching {

            // Remove do map
            crates.remove(id)
            CratePreviewRewardService.remove(crate.identifier)
            CrateHologramService.remove(crate)

            crate.locations
                .mapNotNull { it.value }
                .filter { it.world != null }
                .forEach { location ->
                    location.block.type = Material.AIR
                }

            // Deleta arquivo se existir
            if (file.exists())
            {
                file.delete()
            }

        }.isSuccess
    }

    fun cloneCrate(identifier: String): Crate?
    {
        val original = crates[identifier.lowercase()] ?: return null

        val baseIdentifier = original.identifier.lowercase()
        var copyIndex = 1
        var newIdentifier: String

        do
        {
            newIdentifier = "${baseIdentifier}_copy$copyIndex"
            copyIndex++
        } while (
            crates.containsKey(newIdentifier) ||
            File(SimpleCrates.plugin.dataFolder, "crates/$newIdentifier.yml").exists()
        )

        val clonedRewards = original.rewards.map { reward ->
            Reward(
                identifier = reward.identifier,
                item = reward.item.cloneBuilder(),
                winItems = reward.winItems.map { it.cloneBuilder() }.toMutableList(),
                winCommands = reward.winCommands.toMutableList(),
                winLimit = reward.winLimit,
                weight = reward.weight,
                broadcastMessageEnabled = reward.broadcastMessageEnabled,
                restrictedPermissions = reward.restrictedPermissions.toMutableList()
            )
        }.toMutableList()

        val clonedEngines = mutableMapOf<CrateEngineType, CrateEngine>()
        original.engines.forEach { (type, engine) ->
            val copy = when (engine)
            {
                is VanillaBlock -> VanillaBlock(engine.material)
                is VanillaModel -> VanillaModel(
                    material = engine.material,
                    customModelData = engine.customModelData,
                    rotationDegrees = engine.rotationDegrees,
                    offSetX = engine.offSetX,
                    offSetY = engine.offSetY,
                    offSetZ = engine.offSetZ
                )

                else -> null
            }

            if (copy != null)
            {
                clonedEngines[type] = copy
            }
        }

        val cloned = Crate(
            uuid = UUID.randomUUID(),
            identifier = newIdentifier,
            displayName = "${original.displayName} Copy",
            item = original.item.cloneBuilder(),
            hologram = CrateHologram(
                offset = original.hologram.offset,
                lines = original.hologram.lines.toMutableList()
            ),
            permission = CratePermission(
                key = original.permission.key,
                required = original.permission.required
            ),
            key = CrateKey(
                required = original.key.required,
                ids = original.key.ids.toMutableList()
            ),
            locations = original.locations.map { CrateLocation.deserialize(it.serialize()) }.toMutableList(),
            options = CrateOptions(
                enabled = original.options.enabled,
                previewReward = original.options.previewReward,
                openMoneyCost = original.options.openMoneyCost,
                openCooldownInSeconds = original.options.openCooldownInSeconds
            ),
            rewards = clonedRewards,
            broadcastMessages = original.broadcastMessages.toMutableList(),
            previewRewardSwitchSeconds = original.previewRewardSwitchSeconds,
            previewRewardText = original.previewRewardText,
            previewRewardTextHeight = original.previewRewardTextHeight,
            previewRewardItemHeight = original.previewRewardItemHeight,
            animationEnabled = original.animationEnabled,
            engines = clonedEngines,
            activeEngine = original.activeEngine
        )

        updateCrate(cloned)

        return cloned
    }

    fun updateCrate(crate: Crate): Boolean
    {
        val cratesFolder = File(SimpleCrates.plugin.dataFolder, "crates")

        if (!cratesFolder.exists())
            cratesFolder.mkdirs()

        val file = File(cratesFolder, "${crate.identifier.lowercase()}.yml")
        val config = YamlConfiguration()

        // Data
        config.set("data.uuid", crate.uuid.toString())
        config.set("data.identifier", crate.identifier)
        config.set("data.display-name", crate.displayName)

        // Item
        val itemSerializer = ConfigSerializerRegistry.read(SimpleItemBuilder::class.java)

        val serializedItem = serializeItemWithExtras(itemSerializer, crate.item)

        serializedItem.forEach { (key, value) ->
            config.set("item.$key", value)
        }

        // Hologram
        config.set("hologram.offset", crate.hologram.offset)
        config.set("hologram.lines", crate.hologram.lines)

        // Permission
        config.set("permission.key", crate.permission.key)
        config.set("permission.required", crate.permission.required)

        // Key
        config.set("key.required", crate.key.required)
        config.set("key.ids", crate.key.ids)
        val serializedLocations = crate.locations.mapNotNull { it.serialize() }
        config.set("locations", serializedLocations)

        // Options
        config.set("options.enabled", crate.options.enabled)
        config.set("options.preview-reward", crate.options.previewReward)
        config.set("options.animation-enabled", crate.animationEnabled)
        config.set("options.open-money-cost", crate.options.openMoneyCost)
        config.set("options.open-cooldown-in-seconds", crate.options.openCooldownInSeconds)

        // Engines
        config.set("engine.active", crate.activeEngine.name)

        crate.engines.forEach { (type, engine) ->
            val path = "engine.types.${type.name}"

            when (engine)
            {
                is VanillaBlock ->
                {
                    config.set("$path.material", engine.material.name)
                }

                is VanillaModel ->
                {
                    config.set("$path.material", engine.material.name)
                    config.set("$path.custom-model-data", engine.customModelData)
                    config.set("$path.rotation-degrees", engine.rotationDegrees)
                    config.set("$path.offset-x", engine.offSetX)
                    config.set("$path.offset-y", engine.offSetY)
                    config.set("$path.offset-z", engine.offSetZ)
                }
            }
        }

        // Rewards
        crate.rewards.forEachIndexed { index, reward ->
            val rewardPath = "rewards.$index"

            config.set("$rewardPath.id", reward.id)
            config.set("$rewardPath.identifier", reward.identifier)
            config.set("$rewardPath.win-commands", reward.winCommands)
            config.set("$rewardPath.win-limit", reward.winLimit)
            config.set("$rewardPath.weight", reward.weight)
            config.set("$rewardPath.broadcast-message-enabled", reward.broadcastMessageEnabled)
            config.set("$rewardPath.restricted-permissions", reward.restrictedPermissions)

            val serializedRewardItem = serializeItemWithExtras(itemSerializer, reward.item)
            serializedRewardItem.forEach { (key, value) ->
                config.set("$rewardPath.item.$key", value)
            }

            val serializedWinItems = reward.winItems.map { winItem ->
                serializeItemWithExtras(itemSerializer, winItem)
            }
            config.set("$rewardPath.win-items", serializedWinItems)
        }

        // Broadcast Message
        config.set("broadcast-messages", crate.broadcastMessages)
        config.set("preview-reward.switch-seconds", crate.previewRewardSwitchSeconds)
        config.set("preview-reward.text", crate.previewRewardText)
        config.set("preview-reward.text-height", crate.previewRewardTextHeight)
        config.set("preview-reward.item-height", crate.previewRewardItemHeight)

        return runCatching {
            config.save(file)

            crates[crate.identifier.lowercase()] = crate
            CrateHologramService.refresh(crate)
            CratePreviewRewardService.refresh(crate)
        }.isSuccess
    }

    private fun loadCrate(file: File): Crate
    {

        val config = YamlConfiguration.loadConfiguration(file)

        // Data
        val uuid = config.getString("data.uuid")
            ?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            ?: UUID(0, 0)

        val identifier = config.getString("data.identifier") ?: file.nameWithoutExtension.lowercase()
        val displayName = config.getString("data.display-name") ?: file.nameWithoutExtension.lowercase()

        // Item
        val itemSection = config.getConfigurationSection("item")

        val itemSerializer = ConfigSerializerRegistry.read(SimpleItemBuilder::class.java)

        val item = deserializeSimpleItem(itemSerializer, itemSection)

        // Hologram
        val offSet = config.getDouble("hologram.offset", 2.8000000000000016)
        val lines = config.getStringList("hologram.lines").takeIf { it.isNotEmpty() } ?: mutableListOf(
            "&e&l%name%",
            "",
            "&eGet a key: &7simplesurvival.gg",
            "&7You have &e%simplecrates_key_key_2%&7 Keys."
        )

        val hologram = CrateHologram(offSet, lines)

        // Permission

        val permissionKey = config.getString("permission.key") ?: "simplecrates.crate.${identifier.lowercase()}"
        val permissionRequired = config.getBoolean("permission.required")

        val permission = CratePermission(permissionKey, permissionRequired)

        // Key

        val keyRequired = config.getBoolean("key.required")
        val keyIds = config.getStringList("key.ids")

        val key = CrateKey(keyRequired, keyIds)
        val locations = mutableListOf<CrateLocation>()
        val serializedLocations = config.getStringList("locations")
        if (serializedLocations.isNotEmpty())
        {
            locations.addAll(serializedLocations.map { CrateLocation.deserialize(it) })
        } else
        {
            // Legacy support for single string location.
            val legacyLocation = config.getString("location")
            if (!legacyLocation.isNullOrBlank())
            {
                locations.add(CrateLocation.deserialize(legacyLocation))
            }
        }

        // Options

        val enabled = config.getBoolean("options.enabled", true)

        val openMoneyCost = config.getDouble("options.open-money-cost", 0.0)
        val openCooldownInSeconds = config.getInt("options.open-cooldown-in-seconds", 0)

        val previewReward = config.getBoolean("options.preview-reward", false)
        val animationEnabled = config.getBoolean("options.animation-enabled", false)

        val options = CrateOptions(
            enabled,
            previewReward,
            openMoneyCost,
            openCooldownInSeconds
        )

        // Engines
        val activeEngineType = config.getString("engine.active")
            ?.let { runCatching { CrateEngineType.valueOf(it) }.getOrNull() }
            ?: CrateEngineType.VANILLA_BLOCK

        val engines = mutableMapOf<CrateEngineType, CrateEngine>()

        CrateEngineType.entries.forEach { type ->
            val path = "engine.types.${type.name}"

            val engine = when (type)
            {
                CrateEngineType.VANILLA_BLOCK ->
                {
                    val material = config.getString("$path.material")
                        ?.let { runCatching { Material.valueOf(it) }.getOrNull() }
                        ?: Material.CHEST

                    VanillaBlock(material)
                }

                CrateEngineType.VANILLA_MODEL ->
                {
                    val material = config.getString("$path.material")
                        ?.let { runCatching { Material.valueOf(it) }.getOrNull() }
                        ?: Material.FEATHER

                    VanillaModel(
                        material = material,
                        customModelData = config.getInt("$path.custom-model-data", 0),
                        rotationDegrees = config.getInt("$path.rotation-degrees", 0),
                        offSetX = config.getDouble("$path.offset-x", 0.0),
                        offSetY = config.getDouble("$path.offset-y", 0.0),
                        offSetZ = config.getDouble("$path.offset-z", 0.0)
                    )
                }
            }

            engines[type] = engine
        }

        // Rewards
        val rewards = mutableListOf<Reward>()
        val rewardIds = mutableSetOf<String>()
        val rewardsSection = config.getConfigurationSection("rewards")

        rewardsSection?.getKeys(false)
            ?.sortedBy { it.toIntOrNull() ?: Int.MAX_VALUE }
            ?.forEach { key ->
                val rewardSection = rewardsSection.getConfigurationSection(key) ?: return@forEach
                var rewardId = rewardSection.getString("id")?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
                while (!rewardIds.add(rewardId))
                {
                    rewardId = UUID.randomUUID().toString()
                }
                val rewardIdentifier = rewardSection.getString("identifier") ?: key

                val rewardItemSection = rewardSection.getConfigurationSection("item")
                val rewardItem = deserializeSimpleItem(itemSerializer, rewardItemSection)
                rewardItem.slots.clear()

                val winItems = mutableListOf<SimpleItemBuilder>()
                val rawWinItems = rewardSection.getList("win-items") ?: emptyList<Any>()
                rawWinItems.forEach { raw ->
                    val serializedWinItem = normalizeSerializedItem(raw) ?: return@forEach
                    winItems.add(deserializeSimpleItem(itemSerializer, serializedWinItem))
                }

                rewards.add(
                    Reward(
                        id = rewardId,
                        identifier = rewardIdentifier,
                        item = rewardItem,
                        winItems = winItems,
                        winCommands = rewardSection.getStringList("win-commands"),
                        winLimit = rewardSection.getInt("win-limit", 0),
                        weight = rewardSection.getDouble("weight", 100.0),
                        broadcastMessageEnabled = rewardSection.getBoolean("broadcast-message-enabled", false),
                        restrictedPermissions = rewardSection.getStringList("restricted-permissions")
                    )
                )
            }

        // Broadcast Message
        val broadcastMessages = mutableListOf<String>()

        broadcastMessages.addAll(config.getStringList("broadcast-messages"))

        val previewRewardSwitchSeconds = config.getInt("preview-reward.switch-seconds", 3)
        val previewRewardText = config.getString("preview-reward.text", "&e%reward%") ?: "&e%reward%"
        val previewRewardTextHeight = config.getDouble("preview-reward.text-height", 1.7499999999999996)
        val previewRewardItemHeight = config.getDouble("preview-reward.item-height", 1.4000000000000001)

        // Final result

        return Crate(
            uuid = uuid,
            identifier = identifier,
            displayName = displayName,
            item = item,
            hologram = hologram,
            permission = permission,
            key = key,
            locations = locations,
            options = options,
            rewards = rewards,
            broadcastMessages = broadcastMessages,
            previewRewardSwitchSeconds = previewRewardSwitchSeconds,
            previewRewardText = previewRewardText,
            previewRewardTextHeight = previewRewardTextHeight,
            previewRewardItemHeight = previewRewardItemHeight,
            animationEnabled = animationEnabled,
            engines = engines,
            activeEngine = activeEngineType
        )
    }

    private fun sectionToDeepMap(section: ConfigurationSection?): Map<String, Any?>
    {
        if (section == null) return emptyMap()

        return section.getKeys(false).associateWith { key ->
            val value = section.get(key)
            normalizeYamlValue(value)
        }
    }

    private fun normalizeSerializedItem(raw: Any?): Map<String, Any?>?
    {
        return when (raw)
        {
            is ConfigurationSection -> sectionToDeepMap(raw)
            is Map<*, *> -> normalizeMap(raw)
            else -> null
        }
    }

    private fun normalizeYamlValue(value: Any?): Any?
    {
        return when (value)
        {
            is ConfigurationSection -> sectionToDeepMap(value)
            is Map<*, *> -> normalizeMap(value)
            is List<*> -> value.map { normalizeYamlValue(it) }
            else -> value
        }
    }

    private fun normalizeMap(raw: Map<*, *>): Map<String, Any?>
    {
        return raw.entries.associate { (key, value) ->
            key.toString() to normalizeYamlValue(value)
        }
    }

    private fun deserializeSimpleItem(
        itemSerializer: com.simplesurvival.lib.configuration.serializer.ConfigSerializer<SimpleItemBuilder>,
        section: ConfigurationSection?
    ): SimpleItemBuilder
    {
        return deserializeSimpleItem(itemSerializer, sectionToDeepMap(section))
    }

    private fun deserializeSimpleItem(
        itemSerializer: com.simplesurvival.lib.configuration.serializer.ConfigSerializer<SimpleItemBuilder>,
        serialized: Map<String, Any?>
    ): SimpleItemBuilder
    {
        val builder = itemSerializer.deserializeComplex(serialized)
        val bukkitSerialized = serialized["bukkit-serialized"]
        val map = normalizeSerializedItem(bukkitSerialized)
        if (map != null)
        {
            val itemStack = runCatching { ItemStack.deserialize(map) }.getOrNull()
            if (itemStack != null)
            {
                builder.updateTo(itemStack)
            }
        }
        applySerializedEnchantments(serialized, builder)
        return builder
    }

    private fun serializeItemWithExtras(
        itemSerializer: com.simplesurvival.lib.configuration.serializer.ConfigSerializer<SimpleItemBuilder>,
        item: SimpleItemBuilder
    ): MutableMap<String, Any?>
    {
        val serialized = itemSerializer.serializeComplex(item).toMutableMap()
        serialized["bukkit-serialized"] = serializeBukkitItem(item)
        val enchantments = serializeEnchantments(item)
        if (enchantments.isNotEmpty())
        {
            serialized["enchantments"] = enchantments
        }
        return serialized
    }

    private fun serializeEnchantments(item: ItemStack): Map<String, Int>
    {
        if (item.enchantments.isEmpty()) return emptyMap()

        return item.enchantments.entries.associate { (enchantment, level) ->
            enchantment.key.toString() to level
        }
    }

    private fun applySerializedEnchantments(serialized: Map<String, Any?>, item: SimpleItemBuilder)
    {
        val enchantmentsRaw = serialized["enchantments"] as? Map<*, *> ?: return
        val enchantments = enchantmentsRaw.entries.mapNotNull { (rawKey, rawLevel) ->
            val keyText = rawKey?.toString()?.trim()?.lowercase() ?: return@mapNotNull null
            val level = when (rawLevel)
            {
                is Number -> rawLevel.toInt()
                is String -> rawLevel.toIntOrNull()
                else -> null
            } ?: return@mapNotNull null

            if (level <= 0) return@mapNotNull null
            keyText to level
        }

        if (enchantments.isEmpty()) return

        val existingEnchantments = item.enchantments.keys.toList()
        existingEnchantments.forEach { enchantment ->
            item.removeEnchantment(enchantment)
        }

        enchantments.forEach { (keyText, level) ->
            val namespaced = NamespacedKey.fromString(keyText) ?: NamespacedKey.minecraft(keyText.substringAfter(":"))
            val enchantment = namespaced?.let { Registry.ENCHANTMENT.get(it) }
                ?: runCatching { Enchantment.getByName(keyText.substringAfter(":").uppercase()) }.getOrNull()
                ?: return@forEach

            item.addUnsafeEnchantment(enchantment, level)
        }
    }

    private fun serializeBukkitItem(item: SimpleItemBuilder): Map<String, Any?>
    {
        return normalizeMap(item.serialize())
    }
}
