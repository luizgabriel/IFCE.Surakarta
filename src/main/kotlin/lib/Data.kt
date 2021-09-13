package lib

import java.time.LocalDateTime
import kotlinx.serialization.*
import java.time.ZoneOffset

enum class User {
    ADVERSARY,
    YOU,
    SYSTEM
}

@Serializable
data class TextMessage(
    val content: String,
    val author: User,
    val createdAt: Long = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
)

enum class SocketMessageType {
    TEXT,
    SURRENDER
}

@Serializable
data class SocketMessage(
    val type: SocketMessageType,
    val data: String = ""
) {
    companion object {
        fun ofText(message: String): SocketMessage {
            return SocketMessage(type = SocketMessageType.TEXT, data = message)
        }

        fun ofSurrender(): SocketMessage {
            return SocketMessage(type = SocketMessageType.SURRENDER)
        }
    }
}