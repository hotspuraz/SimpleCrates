package com.simplesurvival.crates.service.key

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.crates.service.crate.CrateService
import com.simplesurvival.lib.configuration.serializer.ConfigSerializerRegistry
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.UUID

object KeyService
{

    val keyMap: MutableMap<String, Key> = mutableMapOf()

    fun init(plugin: JavaPlugin)
    {

        var loaded = 0

        plugin.logger.info { "Loading keys..." }

        keyMap.clear()

        val keyFolder = File(plugin.dataFolder, "keys")

        if (!keyFolder.exists())
        {
            keyFolder.mkdirs()
        }

        keyFolder.listFiles()
            .filter { !it.isDirectory && it.name.endsWith(".yml") }
            .forEach { file ->
                val key = loadKey(file)

                keyMap[key.identifier.lowercase()] = key
                loaded++
            }

        plugin.logger.info { "Loaded $loaded keys." }
    }

    fun get(identifier: String): Key? = keyMap[identifier.lowercase()]

    fun addKey(key: Key): Boolean
    {
        val identifier = key.identifier.lowercase()

        if (keyMap.containsKey(identifier)) return false

        val keyFolder = File(SimpleCrates.plugin.dataFolder, "keys")
        val keyFile = File(keyFolder, "$identifier.yml")

        if (keyFile.exists()) return false

        return if (updateKey(key))
        {
            keyMap[identifier] = key
            true
        } else false
    }

    fun deleteKey(identifier: String): Boolean
    {
        val id = identifier.lowercase()
        val key = keyMap[id] ?: return false

        val file = File(SimpleCrates.plugin.dataFolder, "keys/${id}.yml")

        return runCatching {
            keyMap.remove(id)

            CrateService.crates.values.forEach { crate ->
                if (crate.key.ids.contains(id))
                {
                    crate.key.ids.removeIf { it.equals(id, true) }
                    crate.update()
                }
            }

            if (file.exists())
            {
                file.delete()
            }
        }.isSuccess
    }

    fun renameKey(oldIdentifier: String, newIdentifier: String): Boolean
    {
        val oldId = oldIdentifier.lowercase()
        val newId = newIdentifier.lowercase()

        if (oldId == newId) return true

        val key = keyMap[oldId] ?: return false

        if (keyMap.containsKey(newId)) return false

        val keyFolder = File(SimpleCrates.plugin.dataFolder, "keys")
        val oldFile = File(keyFolder, "$oldId.yml")
        val newFile = File(keyFolder, "$newId.yml")

        if (newFile.exists()) return false

        val renamedKey = Key(
            uuid = key.uuid,
            identifier = newId,
            item = key.item,
            enabled = key.enabled,
            virtual = key.virtual,
            glowing = key.glowing
        )

        return runCatching {
            keyMap.remove(oldId)

            if (!updateKey(renamedKey))
            {
                keyMap[oldId] = key
                return false
            }

            if (oldFile.exists())
            {
                oldFile.delete()
            }

            CrateService.crates.values.forEach { crate ->
                if (crate.key.ids.any { it.equals(oldId, true) })
                {
                    crate.key.ids.removeIf { it.equals(oldId, true) }
                    crate.update()
                }
            }

            keyMap[newId] = renamedKey
        }.isSuccess
    }

    fun cloneKey(identifier: String): Key?
    {
        val original = keyMap[identifier.lowercase()] ?: return null

        val baseIdentifier = original.identifier.lowercase()
        var copyIndex = 1
        var newIdentifier: String

        do
        {
            newIdentifier = "${baseIdentifier}_copy$copyIndex"
            copyIndex++
        } while (
            keyMap.containsKey(newIdentifier) ||
            File(SimpleCrates.plugin.dataFolder, "keys/$newIdentifier.yml").exists()
        )

        val cloned = Key(
            uuid = UUID.randomUUID(),
            identifier = newIdentifier,
            item = original.item,
            enabled = original.enabled,
            virtual = original.virtual,
            glowing = original.glowing
        )

        updateKey(cloned)

        return cloned
    }

    fun updateKey(key: Key): Boolean
    {
        val keyFolder = File(SimpleCrates.plugin.dataFolder, "keys")
        if (!keyFolder.exists())
        {
            keyFolder.mkdirs()
        }

        val file = File(keyFolder, "${key.identifier.lowercase()}.yml")
        val config = YamlConfiguration()

        config.set("uuid", key.uuid.toString())
        config.set("identifier", key.identifier.lowercase())

        val serializer = ConfigSerializerRegistry.read(SimpleItemBuilder::class.java)
        val serializedItem = serializer.serializeComplex(key.item)

        serializedItem.forEach { (itemKey, value) ->
            config.set("item.$itemKey", value)
        }

        config.set("enabled", key.enabled)
        config.set("virtual", key.virtual)
        config.set("glowing", key.glowing)

        return runCatching {
            config.save(file)
            keyMap[key.identifier.lowercase()] = key
        }.isSuccess
    }

    private fun loadKey(file: File): Key
    {

        val config = YamlConfiguration.loadConfiguration(file)

        val uuid = config.getString("uuid")
            ?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            ?: UUID(0, 0)

        val identifier = config.getString("identifier") ?: file.nameWithoutExtension.lowercase()

        // Item

        val serializer = ConfigSerializerRegistry.read(SimpleItemBuilder::class.java)

        val item = serializer.deserializeComplex(
            config.getConfigurationSection("item")?.getValues(false)
        )

        val enabled = config.getBoolean("enabled")
        val virtual = config.getBoolean("virtual")
        val glowing = config.getBoolean("glowing")

        return Key(
            uuid = uuid,
            identifier = identifier,
            item = item,
            enabled = enabled,
            virtual = virtual,
            glowing = glowing
        )
    }
}
