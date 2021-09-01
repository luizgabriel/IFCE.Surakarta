import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.*
import androidx.compose.material.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun MessageInput(modifier: Modifier = Modifier, onSend: (message: String) -> Unit) {
    var inputValue by remember { mutableStateOf("") }

    fun sendMessage() {
        onSend(inputValue)
        inputValue = ""
    }

    Row(modifier) {
        TextField(
            modifier = Modifier.weight(1f),
            value = inputValue,
            onValueChange = { inputValue = it },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions { sendMessage() },
        )
        Button(
            modifier = Modifier.height(56.dp),
            onClick = { sendMessage() },
            enabled = inputValue.isNotBlank(),
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send"
            )
        }
    }
}

enum class User {
    ADVERSARY,
    YOU
}

data class TextMessage(
    val content: String,
    val author: User,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

@Composable
fun MessagesItem(message: TextMessage, modifier: Modifier = Modifier) {
    val formatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val author = when (message.author) {
        User.ADVERSARY -> "Adversário"
        User.YOU -> "Você"
    }
    val backgroundColor = when (message.author) {
        User.YOU -> Color.White
        User.ADVERSARY -> Color(0xFFFFFFE0)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = when (message.author) {
            User.YOU -> Alignment.End
            else -> Alignment.Start
        },
    ) {
        Card(shape = RoundedCornerShape(5.dp), backgroundColor = backgroundColor, elevation = 5.dp, modifier = modifier) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(20.dp).fillMaxWidth(.6f)) {
                Column(Modifier.weight(1.0f)) {
                    Text("${author}:", fontWeight = FontWeight.Bold, fontSize = .9.em)
                    Spacer(Modifier.height(5.dp))
                    Text(message.content)
                }
                Text(message.createdAt.format(formatter), fontSize = .7.em)
            }
        }
    }
}


@Composable
fun MessagesList(messages: List<TextMessage>, modifier: Modifier = Modifier) {
    Column(modifier.padding(20.dp)) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(messages) { message ->
                val align = if (message.author == User.YOU) {
                    Alignment.End
                } else {
                    Alignment.Start
                }
                MessagesItem(message, modifier = Modifier.align(align))
            }
        }
    }
}

@Composable
fun Chat(messages: List<TextMessage>, modifier: Modifier = Modifier, onSend: (message: String) -> Unit) {
    Column(modifier.background(Color.LightGray), verticalArrangement = Arrangement.Bottom) {
        MessagesList(messages, modifier = Modifier.weight(1f))
        MessageInput(Modifier.padding(20.dp)) {
            onSend(it)
        };
    }
}