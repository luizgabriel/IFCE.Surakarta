import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import kotlinx.coroutines.delay

@Composable
@Preview
fun App() {
    var selectedPiece by remember { mutableStateOf(-1) }
    val userTurn by remember { mutableStateOf(User.YOU) }
    val messages = remember { mutableStateListOf<TextMessage>() }
    val boardPieces = remember {
        mutableStateMapOf(
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

    LaunchedEffect(true) {
        var count = 1
        while (true) {
            messages.add(TextMessage("test $count", User.ADVERSARY))
            count++
            delay(10000)
        }
    }

    val onSelectCell = { cell: Int ->
        selectedPiece = cell;
        if (cell != -1) {
            boardPieces.remove(selectedPiece)
            boardPieces[cell] = userTurn
            selectedPiece = -1
        }
    }

    DesktopMaterialTheme {
        Row {
            Board(pieces = boardPieces, selectedCell = selectedPiece, modifier = Modifier.weight(8 / 12.0f),
                onCancel = {
                    selectedPiece = -1
                },
                onTapCell = onSelectCell)
            Chat(messages, modifier = Modifier.weight(4 / 12.0f)) {
                messages.add(TextMessage(it, User.YOU))
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
