package lib

import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.*

@Composable
fun awaitForConnection(port: Int, onConnection: (socket: Socket) -> Unit) {
    val socketServer by remember(port) { mutableStateOf(ServerSocket(port)) }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(socketServer) {
        coroutineScope.launch(Dispatchers.IO) {
            while (true) {
                onConnection(socketServer.accept())
            }
        }

        onDispose {
            socketServer.close()
        }
    }
}

fun Socket.sendMessage(value: String) {
    val writer = PrintWriter(this.outputStream, true)
    writer.println(value)
}

inline fun Socket.messagePool(callback: (message: String) -> Unit) {
    val reader = Scanner(this.inputStream)
    while (reader.hasNextLine()) {
        callback(reader.nextLine())
    }
}