package lib

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import java.time.LocalDateTime
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.time.ZoneOffset

enum class User {
    ADVERSARY,
    YOU,
    SYSTEM
}

enum class Player {
    BLUE,
    RED
}

@Serializable
data class TextMessage(
    val content: String,
    val author: User,
    val createdAt: Long = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
) {
    companion object {
        fun ofConnectedTo(conn: Connection) = TextMessage("Conectado com %s".format(conn), User.SYSTEM)
        fun ofSurrender() = TextMessage("Você desistiu...", User.SYSTEM)
        fun ofAdversarySurrender() = TextMessage("Seu adversário desistiu da partida.", User.SYSTEM)
        fun ofVictory() = TextMessage("Você venceu!", User.SYSTEM)
        fun ofLost() = TextMessage("Você perdeu o jogo...", User.SYSTEM)
        fun ofAcceptingConnections(port: Int) = TextMessage("Aceitando conexões na porta %d".format(port), User.SYSTEM)
        fun ofReset() = TextMessage("O jogo foi reiniciado!", User.SYSTEM)
        fun ofAdversaryReset() =  TextMessage("Seu adversário reiniciou a partida.", User.SYSTEM)
    }
}

data class Connection (
    val host: String,
    val port: Int
) {
    override fun toString(): String {
        return "%s:%d".format(host, port)
    }
}

enum class SocketMessageType {
    TEXT,
    FINISH_GAME,
    SURRENDER,
    MOVE_MOUSE,
    CHANGE_BOARD,
    SELECTED_CELL,
    CHANGE_TURN
}

@Serializable
data class SocketMessage(
    val type: SocketMessageType,
    val data: String = "",
    val position: Pair<Float, Float> = 0f to 0f,
    val board: Map<Int, Player> = mapOf(),
    val cell: Int = -1,
) {
    companion object {
        fun ofText(message: String) = SocketMessage(type = SocketMessageType.TEXT, data = message)
        fun ofSurrender() = SocketMessage(type = SocketMessageType.SURRENDER)
        fun ofFinishGame() = SocketMessage(type = SocketMessageType.FINISH_GAME)
        fun ofMouseMovement(position: Offset) = SocketMessage(type = SocketMessageType.MOVE_MOUSE, position = position.x to position.y)
        fun ofChangeBoard(board: Map<Int, Player>) = SocketMessage(type = SocketMessageType.CHANGE_BOARD, board = board)
        fun ofSelectedCell(cell: Int) = SocketMessage(type = SocketMessageType.SELECTED_CELL, cell = cell)
        fun ofFinishTurn() = SocketMessage(type = SocketMessageType.CHANGE_TURN)
    }
}

fun Player.toColor(): Color {
    return when (this) {
        Player.BLUE -> Color.Blue
        Player.RED -> Color.Red
    }
}

fun Player.toOther(): Player {
    return when (this) {
        Player.BLUE -> Player.RED
        Player.RED -> Player.BLUE
    }
}

inline fun <reified T> T.toJson() = Json.encodeToString(this)