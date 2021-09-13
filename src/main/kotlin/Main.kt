import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import ui.Board
import ui.Chat
import ui.EditConnectionInput
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.random.Random
import kotlinx.serialization.json.*
import lib.*

@OptIn(ExperimentalSerializationApi::class)
@Composable
@Preview
fun App(serverPort: Int = Random.nextInt(8000, 8100)) {
    var selectedPiece by remember { mutableStateOf(-1) }
    val turnUser by remember { mutableStateOf(User.YOU) }
    val messages = remember { mutableStateListOf<TextMessage>() }
    val serverSocket by remember { mutableStateOf(ServerSocket(serverPort)) }
    var adversarySocket by remember { mutableStateOf<Optional<Socket>>(Optional.empty()) }
    val coroutineScope = rememberCoroutineScope()

    val boardPieces = remember {
        mutableStateMapOf(
            *createDefaultSurakartaBoard()
        )
    }

    val onSelectCell = { cell: Int ->
        if (!boardPieces.contains(cell)) {
            boardPieces[cell] = User.YOU
            selectedPiece = -1
        } else if (boardPieces[cell] == User.YOU) {
            boardPieces.remove(cell)
            selectedPiece = cell
        }
    }

    val adversaryConnection = {
        adversarySocket.map { Connection(it.inetAddress.hostAddress, it.port) }
    }

    val addConnectedMessage = {
        val adversaryHost = adversaryConnection().map { it.toString() }.orElse("")
        messages.add(
            TextMessage(
                "Conectado com %s".format(adversaryHost), User.SYSTEM
            )
        )
    }

    val onConnectToAdversary = { adversaryHost: Connection ->
        adversarySocket = Optional.of(Socket(adversaryHost.host, adversaryHost.port))
        addConnectedMessage()
        Unit
    }

    val sendMessageToSocket = { message: SocketMessage ->
        coroutineScope.launch(Dispatchers.IO) {
            adversarySocket.ifPresent {
                Json.encodeToStream(message, it.outputStream)
            }
        }
    }

    val onSendMessage = { text: String ->
        val message = TextMessage(text, User.YOU)
        messages.add(message)
        sendMessageToSocket(SocketMessage.ofText(text))
        Unit
    }

    val onSurrender = {
        messages.add(TextMessage("Você desistiu...", User.SYSTEM))
        sendMessageToSocket(SocketMessage.ofSurrender())
        Unit
    }

    DisposableEffect(Unit) {
        messages.add(TextMessage("Aceitando conexões...", User.SYSTEM))

        coroutineScope.launch(Dispatchers.IO) {
            adversarySocket = Optional.of(serverSocket.accept())
            addConnectedMessage()

            adversarySocket.ifPresent {
                val scanner = Scanner(it.inputStream)
                while (scanner.hasNextLine()) {
                    val message = Json.decodeFromString<SocketMessage>(scanner.nextLine())
                    messages.add(
                        when (message.type) {
                            SocketMessageType.TEXT -> TextMessage(message.data, author = User.ADVERSARY)
                            SocketMessageType.SURRENDER -> TextMessage("O seu adversário desistiu da partida.", User.SYSTEM)
                        }
                    )
                }
            }
        }

        onDispose {
            adversarySocket.ifPresent { it.close() }
        }
    }

    val isConnected = { adversarySocket.map { it.isConnected }.orElse(false) }

    DesktopMaterialTheme {
        Row {
            Board(
                pieces = boardPieces,
                selectedCell = selectedPiece,
                turnUser = turnUser,
                modifier = Modifier.weight(8 / 12.0f),
                onTapCell = onSelectCell
            )
            Column(modifier = Modifier.weight(4 / 12.0f)) {
                EditConnectionInput(
                    port = serverSocket.localPort,
                    adversary = adversaryConnection(),
                    isConnected = isConnected(),
                    onConnect = onConnectToAdversary,
                    onSurrender = onSurrender,
                    modifier = Modifier.height(80.dp)
                )
                Chat(messages, enabled = isConnected(), onSend = onSendMessage)
            }
        }
    }
}

fun main() = application {
    val windowState = WindowState(
        position = WindowPosition.Aligned(Alignment.Center),
        size = WindowSize(1280.dp, 720.dp),
    )

    Window(
        title = "Surakarta",
        state = windowState,
        onCloseRequest = ::exitApplication
    ) {
        App()
    }
}
