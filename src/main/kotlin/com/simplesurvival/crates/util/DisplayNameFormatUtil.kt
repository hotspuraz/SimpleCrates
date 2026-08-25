package com.simplesurvival.crates.util

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

object DisplayNameFormatUtil
{
    private val miniMessage = MiniMessage.miniMessage()
    private val legacySerializer = LegacyComponentSerializer.legacyAmpersand()
    private val plainSerializer = PlainTextComponentSerializer.plainText()
    private val miniTagRegex = Regex("<[^>]+>")
    private val brokenTagPrefixRegex = Regex("</!")

    fun normalize(rawName: String): String
    {
        if (rawName.isBlank()) return rawName

        if ('<' !in rawName || '>' !in rawName)
        {
            return rawName
        }

        val sanitizedInput = rawName
            .replace(brokenTagPrefixRegex, "</")

        val serialized = runCatching {
            legacySerializer.serialize(miniMessage.deserialize(sanitizedInput))
        }.getOrElse {
            // If MiniMessage content is malformed, fallback to visible plain text without raw tags.
            val withoutTags = sanitizedInput.replace(miniTagRegex, "")
            if (withoutTags.isNotBlank()) withoutTags else plainSerializer.serialize(miniMessage.deserialize("<gray>Invalid Name"))
        }

        // In lenient MiniMessage mode invalid/unsupported close tags may leak as literal text.
        return serialized
            .replace(miniTagRegex, "")
            .replace("</italic>", "")
            .replace("</bold>", "")
            .replace("</underlined>", "")
            .replace("</strikethrough>", "")
            .replace("</obfuscated>", "")
    }

}
