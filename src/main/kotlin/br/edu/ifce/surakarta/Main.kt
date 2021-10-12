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
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import br.edu.ifce.surakarta.*
import br.edu.ifce.surakarta.lib.ISurakartaPlayer
import br.edu.ifce.surakarta.lib.exportSurakartaPlayer
import br.edu.ifce.surakarta.lib.findSurakartaPlayer
import lib.*
import br.edu.ifce.surakarta.ui.*
import java.lang.Exception
import java.net.InetAddress
import java.rmi.RemoteException
import java.rmi.registry.LocateRegistry
import java.rmi.server.UnicastRemoteObject
import kotlin.random.Random

@Composable
@Preview
fun App(serverPort: Int = Random.nextInt(8000, 8100)) {
    var selectedCell by remember { mutableStateOf(-1) }
    var turnPlayer by remember { mutableStateOf(Player.BLUE) }
    var yourPlayer by remember { mutableStateOf(Player.BLUE) }
    var adversaryMousePosition by remember { mutableStateOf(Offset(0f, 0f)) }
    val messages = remember { mutableStateListOf<TextMessage>() }
    val boardPieces = remember { mutableStateMapOf<Int, Player>() }
    var winner by remember { mutableStateOf<Player?>(null) }
    var adversaryConnection by remember { mutableStateOf<Connection?>(null) }

    var adversary by remember { mutableStateOf<ISurakartaPlayer?>(null) }

    val resetBoard = {
        boardPieces.clear()
        boardPieces.putAll(createDefaultSurakartaBoard())
    }

    val disconnect = { e: Exception ->
        e.printStackTrace();
        messages.add(TextMessage.ofError(e.message))
        adversary = null
        adversaryConnection = null
    }

    LaunchedEffect(serverPort) {
        exportSurakartaPlayer(serverPort, object : UnicastRemoteObject(), ISurakartaPlayer {

            override fun start(host: String, port: Int) {
                yourPlayer = Player.RED
                adversaryConnection = Connection(host, port)
                adversary = findSurakartaPlayer(adversaryConnection!!);
                messages.add(TextMessage.ofConnectedTo(adversaryConnection!!))
                resetBoard()
            }

            override fun sendMessage(text: String) {
                messages.add(TextMessage(text, author = User.ADVERSARY))
            }

            override fun moveMouse(positionX: Float, positionY: Float) {
                adversaryMousePosition = Offset(positionX, positionY)
            }

            override fun changeBoard(board: HashMap<Int, Player>) {
                boardPieces.clear()
                boardPieces.putAll(board)
                winner = findWinner(boardPieces)
            }

            override fun selectCell(cell: Int) {
                selectedCell = cell
            }

            override fun changeTurn() {
                turnPlayer = turnPlayer.toOther()
            }

            override fun finishGame() {
                messages.add(TextMessage.ofAdversaryReset())
                resetBoard()
            }

            override fun surrender() {
                messages.add(TextMessage.ofAdversarySurrender())
                resetBoard()
            }
        })
    }

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

        winner = findWinner(boardPieces)

        try {
            adversary?.selectCell(selectedCell)
            adversary?.changeBoard(HashMap(boardPieces))
        } catch (e: Exception) {
            disconnect(e);
        }

        Unit
    }

    val onConnectToAdversary = { adversaryHost: Connection ->
        yourPlayer = Player.BLUE
        adversaryConnection = adversaryHost

        try {
            val registry = LocateRegistry.getRegistry(adversaryHost.host, adversaryHost.port)
            adversary = registry.lookup("SurakartaPlayer") as ISurakartaPlayer;
            adversary?.start(InetAddress.getLocalHost().hostAddress, serverPort)
            resetBoard()
        } catch (e: Exception) {
            e.printStackTrace()
            adversaryConnection = null
        }

        Unit
    }

    val onSendMessage = { text: String ->
        messages.add(TextMessage(text, User.YOU))
        adversary?.sendMessage(text)
        Unit
    }

    val onCursorMove = { position: Offset ->
        if (turnPlayer == yourPlayer)
            adversary?.moveMouse(position.x, position.y)
        Unit
    }

    val onFinishTurn = {
        turnPlayer = turnPlayer.toOther()
        try {
            adversary?.changeTurn()
        } catch (e: Exception) {
            disconnect(e)
        }
        Unit
    }

    val onSurrender = {
        try {
            if (winner == null) {
                messages.add(TextMessage.ofSurrender())
                adversary?.surrender()
                onFinishTurn()
            } else {
                messages.add(TextMessage.ofReset())
                adversary?.finishGame()
            }
        } catch (e: Exception) {
            disconnect(e);
        }

        resetBoard()
    }

    LaunchedEffect(serverPort) {
        messages.add(TextMessage.ofAcceptingConnections(serverPort))
    }

    LaunchedEffect(winner) {
        if (winner == yourPlayer) {
            messages.add(TextMessage.ofVictory())
        } else if (winner == yourPlayer.toOther()) {
            messages.add(TextMessage.ofLost())
        }
    }

    val isConnected = adversary != null
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
