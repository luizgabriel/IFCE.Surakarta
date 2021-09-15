package lib

import java.net.Socket
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

fun findWinner(board: Map<Int, Player>): Player? {
    var countBlue = 0
    var countRed = 0

    for ((_, player) in board) {
        when (player) {
            Player.RED -> countRed++
            Player.BLUE -> countBlue++
        }
    }

    return if (countBlue == 0)
        Player.RED
    else if (countRed == 0)
        Player.BLUE
    else
        null
}

fun Socket.toConnection() = Connection(this.inetAddress.hostAddress, this.port)