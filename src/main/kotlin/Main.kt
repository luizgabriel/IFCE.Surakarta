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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
    var selectedCell by remember { mutableStateOf(-1) }
    var turnPlayer by remember { mutableStateOf(Player.BLUE) }
    var yourPlayer by remember { mutableStateOf(Player.BLUE) }
    var adversaryMousePosition by remember { mutableStateOf(Offset(0f, 0f)) }
    val messages = remember { mutableStateListOf<TextMessage>() }
    var adversarySocket by remember { mutableStateOf<Socket?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val boardPieces = remember { mutableStateMapOf<Int, Player>() }
    var winner by remember { mutableStateOf<Player?>(null) }

    val onSelectCell = { cell: Int ->
        if (!boardPieces.contains(cell) && selectedCell >= 0) {
            boardPieces[cell] = yourPlayer
            selectedCell = -1
        } else if (boardPieces[cell] == yourPlayer && selectedCell == -1) {
            boardPieces.remove(cell)
            selectedCell = cell
        } else if (boardPieces[cell] == yourPlayer.toOther() && selectedCell >= 0) {
            boardPieces[cell] = yourPlayer
            selectedCell = -1
        }
    }

    val resetBoard = {
        boardPieces.clear()
        boardPieces.putAll(createDefaultSurakartaBoard())
    }

    val onReceiveMessage = { message: SocketMessage ->
        when (message.type) {
            SocketMessageType.SURRENDER -> {
                messages.add(TextMessage.ofAdversarySurrender())
                resetBoard()
            }
            SocketMessageType.FINISH_GAME -> {
                messages.add(TextMessage.ofAdversaryReset())
                resetBoard()
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
                winner = findWinner(boardPieces)
            }
            SocketMessageType.SELECTED_CELL -> {
                selectedCell = message.cell
            }
        }
        Unit
    }

    val onConnect = { player: Player, socket: Socket ->
        val connection = socket.toConnection()
        adversarySocket = socket
        yourPlayer = player
        messages.add(TextMessage.ofConnectedTo(connection))
        boardPieces.clear()
        boardPieces.putAll(createDefaultSurakartaBoard())

        socket.messagePool { onReceiveMessage(Json.decodeFromString(it)) }
    }

    val onConnectToAdversary = { adversaryHost: Connection ->
        coroutineScope.launch(Dispatchers.IO) {
            val socket = Socket(adversaryHost.host, adversaryHost.port)
            onConnect(Player.BLUE, socket)
        }
        Unit
    }

    val sendMessageToSocket = { message: SocketMessage ->
        adversarySocket?.sendMessage(message.toJson())
    }

    val onSendMessage = { text: String ->
        val message = TextMessage(text, User.YOU)
        messages.add(message)
        sendMessageToSocket(SocketMessage.ofText(text))
        Unit
    }

    val onCursorMove = { position: Offset ->
        coroutineScope.launch {
            sendMessageToSocket(SocketMessage.ofMouseMovement(position))
        }
        Unit
    }

    val onFinishTurn = {
        turnPlayer = turnPlayer.toOther()
        sendMessageToSocket(SocketMessage.ofFinishTurn())
        Unit
    }

    val onSurrender = {
        if (winner == null) {
            messages.add(TextMessage.ofSurrender())
            sendMessageToSocket(SocketMessage.ofSurrender())
            onFinishTurn()
        } else {
            messages.add(TextMessage.ofReset())
            sendMessageToSocket(SocketMessage.ofFinishGame())
        }

        boardPieces.clear()
        boardPieces.putAll(createDefaultSurakartaBoard())
    }

    LaunchedEffect(serverPort) {
        messages.add(TextMessage.ofAcceptingConnections(serverPort))
    }

    awaitForConnection(serverPort) {
        onConnect(Player.RED, it)
    }

    LaunchedEffect(selectedCell) {
        sendMessageToSocket(SocketMessage.ofSelectedCell(selectedCell))
    }

    LaunchedEffect(boardPieces.toJson<Map<Int, Player>>()) {
        sendMessageToSocket(SocketMessage.ofChangeBoard(boardPieces))
    }

    LaunchedEffect(winner) {
        if (winner == yourPlayer) {
            messages.add(TextMessage.ofVictory())
        } else if (winner == yourPlayer.toOther()) {
            messages.add(TextMessage.ofLost())
        }
    }

    val isConnected = adversarySocket != null && adversarySocket!!.isConnected
    val adversaryConnection = adversarySocket?.toConnection()
    val enabledBoard = turnPlayer == yourPlayer && isConnected && winner == null

    DesktopMaterialTheme {
        Row {
            Column(modifier = Modifier.padding(5.dp).weight(8 / 12.0f)) {
                Button(onClick = onFinishTurn, enabled = enabledBoard) {
                    Text("Terminar Turno")
                }
                Board(
                    pieces = boardPieces,
                    selectedCell = selectedCell,
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
                    port = serverPort,
                    adversary = adversaryConnection,
                    isConnected = isConnected,
                    onConnect = onConnectToAdversary,
                    onSurrender = onSurrender,
                    winner = winner,
                    modifier = Modifier.height(80.dp)
                )
                Chat(messages, enabled = isConnected, onSend = onSendMessage)
            }
        }
    }
}

fun main() = application {
    val windowState = rememberWindowState(position = WindowPosition.Aligned(Alignment.Center), width = 1280.dp, height = 720.dp)

    Window(
        title = "Surakarta",
        state = windowState,
        onCloseRequest = ::exitApplication
    ) {
        App()
    }
}
