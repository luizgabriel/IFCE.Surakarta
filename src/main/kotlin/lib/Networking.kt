package lib

import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.ServerSocket
import java.net.Socket

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