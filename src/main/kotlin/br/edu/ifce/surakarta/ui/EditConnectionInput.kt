package br.edu.ifce.surakarta.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import lib.Connection
import lib.Player
import lib.parseConnectionString

@Composable
fun EditConnectionInput(
    port: Int,
    winner: Player?,
    isConnected: Boolean,
    adversary: Connection? = null,
    onConnect: (connection: Connection) -> Unit,
    onSurrender: () -> Unit,
    modifier: Modifier = Modifier
) {
    var connection by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val onClickConnect = {
        parseConnectionString(connection).onFailure {
            error = it.toString()
        }.onSuccess {
            onConnect(it)
        }
    }

    val connectionValue = adversary?.toString() ?: connection

    Row(modifier = modifier.fillMaxWidth().padding(5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.padding(0.dp, 5.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text("Escutando conexões na porta: %d".format(port), color = Color.DarkGray, fontSize = .8.em)
            TextField(
                enabled = adversary == null,
                value = connectionValue,
                onValueChange = { connection = it },
                placeholder = {
                    Text("0.0.0.0:9999")
                })
            if (error.isNotEmpty())
                Text(error, color = Color.Red)
        }
        Column(
            Modifier.fillMaxHeight().padding(5.dp, 0.dp, 0.dp, 0.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Button(enabled = !isConnected, modifier = Modifier.fillMaxWidth(), onClick = {
                onClickConnect()
            }) {
                Text("Conectar")
            }
            Spacer(modifier = Modifier.height(4.dp))
            Button(enabled = isConnected, modifier = Modifier.fillMaxWidth(), onClick = onSurrender) {
                Text(if (winner != null) "Reiniciar Partida" else "Desistir")
            }
        }
    }
}