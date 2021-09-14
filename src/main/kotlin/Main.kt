import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import kotlinx.coroutines.*
import ui.Board
import ui.Chat
import ui.EditConnectionInput
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.random.Random
import lib.*

@Composable
@Preview
fun App(serverPort: Int = Random.nextInt(8000, 8100)) {
    var selectedPiece by remember { mutableStateOf(-1) }
    var turnPlayer by remember { mutableStateOf(Player.BLUE) }
    var yourPlayer by remember { mutableStateOf(Player.BLUE) }
    var adversaryMousePosition by remember { mutableStateOf(Offset(0f, 0f)) }
    val messages = remember { mutableStateListOf<TextMessage>() }
    val serverSocket by remember { mutableStateOf(ServerSocket(serverPort)) }
    var adversarySocket by remember { mutableStateOf<Optional<Socket>>(Optional.empty()) }
    val coroutineScope = rememberCoroutineScope()
    val boardPieces = remember { mutableStateMapOf<Int, Player>() }

    val onSelectCell = { cell: Int ->
        if (!boardPieces.contains(cell) && selectedPiece >= 0) {
            boardPieces[cell] = yourPlayer
            selectedPiece = -1
        } else if (boardPieces[cell] == yourPlayer && selectedPiece == -1) {
            boardPieces.remove(cell)
            selectedPiece = cell
        } else if (boardPieces[cell] == yourPlayer.toOther() && selectedPiece >= 0) {
            boardPieces[cell] = yourPlayer
            selectedPiece = -1
        }

        adversarySocket.ifPresent {
            it.sendJsonMessage(SocketMessage.ofChangeBoard(boardPieces))
        }
    }

    val onReceiveMessage = { message: SocketMessage ->
        when (message.type) {
            SocketMessageType.SURRENDER -> {
                messages.add(TextMessage.ofAdversarySurrender())
                boardPieces.clear()
                boardPieces.putAll(createDefaultSurakartaBoard())
            }
            SocketMessageType.TEXT -> messages.add(TextMessage(message.data, author = User.ADVERSARY))
            SocketMessageType.MOVE_MOUSE -> {
                adversaryMousePosition = Offset(message.position.first, message.position.second)
            }
            SocketMessageType.CHANGE_TURN -> {
                turnPlayer = turnPlayer.toOther()
            }
            SocketMessageType.CHANGE_BOARD -> {
                boardPieces.clear()
                boardPieces.putAll(message.board)
            }
            SocketMessageType.SELECTED_CELL -> {
                selectedPiece = message.cell
            }
        }
        Unit
    }

    val onConnect = { player: Player, socket: Socket ->
        val connection = socket.toConnection()
        coroutineScope.launch(Dispatchers.Main) {
            messages.add(TextMessage.ofConnectedTo(connection))
            adversarySocket = Optional.of(socket)
            yourPlayer = player
            boardPieces.clear()
            boardPieces.putAll(createDefaultSurakartaBoard())
        }
        socket.jsonMessagePool(onReceiveMessage)
    }

    val onConnectToAdversary = { adversaryHost: Connection ->
        coroutineScope.launch(Dispatchers.IO) {
            val socket = Socket(adversaryHost.host, adversaryHost.port)
            onConnect(Player.BLUE, socket)
        }
        Unit
    }

    val sendMessageToSocket = { message: SocketMessage ->
        coroutineScope.launch(Dispatchers.IO) {
            adversarySocket.ifPresent {
                it.sendJsonMessage(message)
            }
        }
        Unit
    }

    val onSendMessage = { text: String ->
        val message = TextMessage(text, User.YOU)
        messages.add(message)
        sendMessageToSocket(SocketMessage.ofText(text))
    }

    val onCursorMove = { position: Offset ->
        sendMessageToSocket(SocketMessage.ofMouseMovement(position))
    }

    val onSurrender = {
        messages.add(TextMessage.ofSurrender())
        sendMessageToSocket(SocketMessage.ofSurrender())
        boardPieces.clear()
        boardPieces.putAll(createDefaultSurakartaBoard())
    }

    val onFinishTurn = {
        turnPlayer = turnPlayer.toOther()
        sendMessageToSocket(SocketMessage.ofFinishTurn())
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            serverSocket.connectionPool {
                onConnect(Player.RED, it)
            }
        }
    }

    val isConnected = adversarySocket.map { it.isConnected }.orElse(false)
    val adversaryConnection = adversarySocket.map { it.toConnection() }
    val enabledBoard = turnPlayer == yourPlayer && isConnected;

    DesktopMaterialTheme {
        Row {
            Column(modifier = Modifier.padding(5.dp).weight(8 / 12.0f)) {
                Button(onClick = onFinishTurn, enabled = enabledBoard) {
                    Text("Terminar Turno")
                }
                Board(
                    pieces = boardPieces,
                    selectedCell = selectedPiece,
                    turnPlayer = turnPlayer,
                    adversaryMousePosition = adversaryMousePosition,
                    enabled = enabledBoard,
                    modifier = Modifier.fillMaxWidth(),
                    onTapCell = onSelectCell,
                    onCursorMove = onCursorMove
                )
            }
            Column(modifier = Modifier.weight(4 / 12.0f)) {
                EditConnectionInput(
                    port = serverSocket.localPort,
                    adversary = adversaryConnection,
                    isConnected = isConnected,
                    onConnect = onConnectToAdversary,
                    onSurrender = onSurrender,
                    modifier = Modifier.height(80.dp)
                )
                Chat(messages, enabled = isConnected, onSend = onSendMessage)
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
