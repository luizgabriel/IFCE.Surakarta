package lib

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Error
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.random.Random

fun createDefaultSurakartaBoard(): Map<Int, Player> {
    return mapOf(
        0 to Player.BLUE,
        1 to Player.BLUE,
        2 to Player.BLUE,
        3 to Player.BLUE,
        4 to Player.BLUE,
        5 to Player.BLUE,
        6 to Player.BLUE,
        7 to Player.BLUE,
        8 to Player.BLUE,
        9 to Player.BLUE,
        10 to Player.BLUE,
        11 to Player.BLUE,

        24 to Player.RED,
        25 to Player.RED,
        26 to Player.RED,
        27 to Player.RED,
        28 to Player.RED,
        29 to Player.RED,
        30 to Player.RED,
        31 to Player.RED,
        32 to Player.RED,
        33 to Player.RED,
        34 to Player.RED,
        35 to Player.RED,
    )
}

data class Connection (
    val host: String,
    val port: Int
) {
    override fun toString(): String {
        return "%s:%d".format(host, port)
    }
}

fun parseConnectionString(connection: String, defaultPort: Int = Random.nextInt(8001, 8100)): Result<Connection> {
    val parts = connection.split(':');
    return if (parts.size == 1) {
        Result.success(Connection(parts[0], defaultPort))
    } else if (parts.size >= 2) {
        Result.success(Connection(parts[0], parts[1].toInt(10)))
    } else {
        Result.failure(Error("Invalid connection string"));
    }
}

fun Socket.toConnection() = Connection(this.inetAddress.hostAddress, this.port)

inline fun <reified T> Socket.sendJsonMessage(value : T) {
    val writer = PrintWriter(this.outputStream, true)
    writer.println(Json.encodeToString(value))
}

inline fun <reified T> Socket.jsonMessagePool(callback : (message: T) -> Unit) {
    val writer = PrintWriter(this.outputStream, true)
    val reader = BufferedReader(InputStreamReader(this.inputStream))
    while (true) {
        callback(Json.decodeFromString<T>(reader.readLine()))
    }
}

inline fun ServerSocket.connectionPool(callback : (socket: Socket) -> Unit) {
    while (true) {
        callback(this.accept())
    }
}