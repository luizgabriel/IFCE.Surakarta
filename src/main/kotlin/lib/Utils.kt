package lib

import java.lang.Error
import kotlin.random.Random

fun createDefaultSurakartaBoard(): Array<Pair<Int, User>> {
    return arrayOf(
        0 to User.YOU,
        1 to User.ADVERSARY,
        2 to User.YOU,
        3 to User.ADVERSARY,
        4 to User.YOU,
        5 to User.ADVERSARY,
        6 to User.YOU,
        7 to User.ADVERSARY,
        8 to User.YOU,
        9 to User.ADVERSARY,
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