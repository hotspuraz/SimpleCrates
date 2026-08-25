package com.simplesurvival.crates.service.chat

import java.util.UUID
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val playerId: UUID,
    val key: ChatMessageKey,
    val identifier: String,
    val rewardId: String = "",
    val otherInformation: String = "",
    val expiresAt: Long = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1)
)
{
    fun hasExpired() = expiresAt <= System.currentTimeMillis()
}
