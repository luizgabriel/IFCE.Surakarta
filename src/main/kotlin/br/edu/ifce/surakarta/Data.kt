package br.edu.ifce.surakarta

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import java.rmi.server.UnicastRemoteObject
import java.time.LocalDateTime
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
        fun ofError(message: String?) = TextMessage("Ocorrey um erro: $message", User.SYSTEM)
    }
}

@Serializable
data class Connection (
    val host: String,
    val port: Int
): UnicastRemoteObject() {
    override fun toString(): String {
        return "%s:%d".format(host, port)
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