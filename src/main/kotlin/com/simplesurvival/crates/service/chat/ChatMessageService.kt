package com.simplesurvival.crates.service.chat

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object ChatMessageService
{

    val cache: MutableMap<UUID, ChatMessage> = ConcurrentHashMap()

    fun add(chat: ChatMessage)
    {
        cache[chat.playerId] = chat
    }

    fun remove(playerId: UUID) = cache.remove(playerId)

    fun get(playerId: UUID): ChatMessage? = cache[playerId]

    fun entriesSnapshot(): List<Map.Entry<UUID, ChatMessage>> = cache.entries.toList()
}
