package br.edu.ifce.surakarta.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import lib.TextMessage
import lib.User
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun MessageInput(enabled: Boolean, modifier: Modifier = Modifier, onSend: (message: String) -> Unit) {
    var inputValue by remember { mutableStateOf("") }

    fun sendMessage() {
        onSend(inputValue)
        inputValue = ""
    }

    val baseModifier = Modifier.height(46.dp)

    Row(modifier) {
        TextField(
            enabled = enabled,
            modifier = baseModifier.weight(1f),
            value = inputValue,
            onValueChange = { inputValue = it },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions { sendMessage() },
        )
        Button(
            modifier = baseModifier,
            onClick = { sendMessage() },
            enabled = enabled && inputValue.isNotBlank(),
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send"
            )
        }
    }
}


@Composable
fun MessagesItem(message: TextMessage, modifier: Modifier = Modifier) {
    val formatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val author = when (message.author) {
        User.ADVERSARY -> "Adversário"
        User.YOU -> "Você"
        User.SYSTEM -> "Sistema"
    }
    val backgroundColor = when (message.author) {
        User.YOU -> Color.White
        else -> Color(0xFFFFFFE0)
    }

    val horizontalAlignment = when (message.author) {
        User.YOU -> Alignment.End
        else -> Alignment.Start
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = horizontalAlignment
    ) {
        Card(shape = RoundedCornerShape(5.dp), backgroundColor = backgroundColor, elevation = 5.dp) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp).fillMaxWidth(.6f)) {
                Column(Modifier.weight(1.0f)) {
                    Text("${author}:", fontWeight = FontWeight.Bold, fontSize = .8.em, color = Color.Gray)
                    Spacer(Modifier.height(2.dp))
                    Text(message.content)
                }
                Text(LocalDateTime.ofEpochSecond(message.createdAt, 0, ZoneOffset.UTC).format(formatter), fontSize = .7.em)
            }
        }
    }
}


@Composable
fun MessagesList(messages: List<TextMessage>, modifier: Modifier = Modifier) {
    Column(modifier.padding(20.dp)) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
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
fun Chat(messages: List<TextMessage>, enabled: Boolean, modifier: Modifier = Modifier, onSend: (message: String) -> Unit) {
    Column(modifier.background(Color.LightGray), verticalArrangement = Arrangement.Bottom) {
        MessagesList(messages, modifier = Modifier.weight(1f))
        MessageInput(enabled = enabled, Modifier.padding(20.dp)) {
            onSend(it)
        };
    }
}