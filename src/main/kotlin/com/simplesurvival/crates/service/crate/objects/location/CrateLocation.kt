package com.simplesurvival.crates.service.crate.objects.location

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import java.util.UUID

class CrateLocation(
    value: Location? = null,
    private var serializedFallback: String? = null
)
{

    private var currentValue: Location? = value

    val value: Location?
        get() = resolve()

    init
    {
        if (serializedFallback.isNullOrBlank() && value?.world != null)
        {
            serializedFallback = serializeResolved(value)
        }
    }

    fun update(newValue: Location?)
    {
        currentValue = newValue
        if (newValue?.world != null)
        {
            serializedFallback = serializeResolved(newValue)
        }
    }

    fun isValid(): Boolean = resolve() != null

    fun sameBlock(other: Location): Boolean
    {
        val current = resolve() ?: return false
        val world = current.world ?: return false
        val otherWorld = other.world ?: return false

        return world.uid == otherWorld.uid &&
            current.blockX == other.blockX &&
            current.blockY == other.blockY &&
            current.blockZ == other.blockZ
    }

    fun hologramBase(offset: Double): Location?
    {
        val location = resolve() ?: return null
        val world = location.world ?: return null

        return Location(
            world,
            location.blockX + 0.5,
            location.blockY + offset,
            location.blockZ + 0.5
        )
    }

    fun serialize(): String?
    {
        val location = resolve()
        if (location?.world != null)
        {
            val serialized = serializeResolved(location)
            serializedFallback = serialized
            return serialized
        }

        return serializedFallback?.takeIf { it.isNotBlank() }
    }

    private fun resolve(): Location?
    {
        val current = currentValue
        if (current?.world != null)
        {
            return current
        }

        val fallback = serializedFallback ?: return null
        val parsed = parseSerialized(fallback) ?: return null
        val world = parsed.resolveWorld() ?: return null

        val resolved = Location(world, parsed.x, parsed.y, parsed.z, parsed.yaw, parsed.pitch)
        currentValue = resolved
        serializedFallback = parsed.normalized()

        return resolved
    }

    companion object
    {
        private const val FORMAT_V2 = "v2"

        fun deserialize(serialized: String?): CrateLocation
        {
            if (serialized.isNullOrBlank()) return CrateLocation()

            val parsed = parseSerialized(serialized) ?: return CrateLocation(serializedFallback = serialized)
            val world = parsed.resolveWorld()
            val normalized = parsed.normalized()

            if (world == null)
            {
                return CrateLocation(serializedFallback = normalized)
            }

            return CrateLocation(
                value = Location(world, parsed.x, parsed.y, parsed.z, parsed.yaw, parsed.pitch),
                serializedFallback = normalized
            )
        }

        private fun serializeResolved(location: Location?): String?
        {
            val world = location?.world ?: return null
            return listOf(
                FORMAT_V2,
                world.uid.toString(),
                world.name,
                location.x.toString(),
                location.y.toString(),
                location.z.toString(),
                location.yaw.toString(),
                location.pitch.toString()
            ).joinToString(";")
        }

        private fun parseSerialized(serialized: String): ParsedCrateLocation?
        {
            val parts = serialized.split(";")
            if (parts.isEmpty()) return null

            if (parts.first().equals(FORMAT_V2, ignoreCase = true))
            {
                if (parts.size < 8) return null

                val worldUid = parts.getOrNull(1)?.let { runCatching { UUID.fromString(it) }.getOrNull() }
                val worldName = parts.getOrNull(2)?.takeIf { it.isNotBlank() }
                val x = parts.getOrNull(3)?.toDoubleOrNull() ?: return null
                val y = parts.getOrNull(4)?.toDoubleOrNull() ?: return null
                val z = parts.getOrNull(5)?.toDoubleOrNull() ?: return null
                val yaw = parts.getOrNull(6)?.toFloatOrNull() ?: 0f
                val pitch = parts.getOrNull(7)?.toFloatOrNull() ?: 0f

                return ParsedCrateLocation(
                    worldUid = worldUid,
                    worldName = worldName,
                    x = x,
                    y = y,
                    z = z,
                    yaw = yaw,
                    pitch = pitch
                )
            }

            // Legacy format: worldName;x;y;z(;yaw;pitch)
            if (parts.size < 4) return null

            val worldName = parts.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return null
            val x = parts.getOrNull(1)?.toDoubleOrNull() ?: return null
            val y = parts.getOrNull(2)?.toDoubleOrNull() ?: return null
            val z = parts.getOrNull(3)?.toDoubleOrNull() ?: return null
            val yaw = parts.getOrNull(4)?.toFloatOrNull() ?: 0f
            val pitch = parts.getOrNull(5)?.toFloatOrNull() ?: 0f

            return ParsedCrateLocation(
                worldUid = null,
                worldName = worldName,
                x = x,
                y = y,
                z = z,
                yaw = yaw,
                pitch = pitch
            )
        }

        private data class ParsedCrateLocation(
            val worldUid: UUID?,
            val worldName: String?,
            val x: Double,
            val y: Double,
            val z: Double,
            val yaw: Float,
            val pitch: Float
        )
        {
            fun resolveWorld(): World?
            {
                val byUid = worldUid?.let { Bukkit.getWorld(it) }
                if (byUid != null)
                {
                    return byUid
                }

                return worldName?.let { Bukkit.getWorld(it) }
            }

            fun normalized(): String
            {
                val resolved = resolveWorld()
                val normalizedWorldName = resolved?.name ?: worldName.orEmpty()
                val normalizedWorldUid = resolved?.uid?.toString() ?: worldUid?.toString().orEmpty()

                return listOf(
                    FORMAT_V2,
                    normalizedWorldUid,
                    normalizedWorldName,
                    x.toString(),
                    y.toString(),
                    z.toString(),
                    yaw.toString(),
                    pitch.toString()
                ).joinToString(";")
            }
        }
    }
}
