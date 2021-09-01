import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay

@Composable
@Preview
fun App() {
    val messages = remember { mutableStateListOf<TextMessage>() }
    val boardImage = painterResource("board.png")

    LaunchedEffect(true) {
        var count = 1
        while (true) {
            messages.add(TextMessage("test $count", User.ADVERSARY))
            count++
            delay(5000)
        }
    }

    DesktopMaterialTheme {
        Row {
            Column(Modifier.padding(20.dp).fillMaxHeight()) {
                Icon(boardImage, "Board")
            }
            Chat(messages, Modifier.fillMaxSize()) {
                messages.add(TextMessage(it, User.YOU))
            }
        }
    }
}

fun main() = application {
    val state = WindowState(width = 1280.dp, height = 720.dp)
    Window(title = "Surakarta", state = state, onCloseRequest = ::exitApplication) {
        App()
    }
}
