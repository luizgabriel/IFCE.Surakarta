package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import lib.User
import kotlin.math.max
import kotlin.math.min

private fun toColor(user: User): Color {
    return when (user) {
        User.YOU -> Color.Blue
        User.ADVERSARY -> Color.Red
        else -> Color.Cyan
    }
}

private fun isInside(tapPosition: Offset, centerPosition: Offset, radius: Float): Boolean {
    val topLeftPosition = centerPosition - Offset(2f, 2f) * radius
    val bottomRightPosition = centerPosition + Offset(2f, 2f) * radius

    return (tapPosition.x > topLeftPosition.x)
            && (tapPosition.y > topLeftPosition.y)
            && (tapPosition.x < bottomRightPosition.x)
            && (tapPosition.y < bottomRightPosition.y)
}

@Composable
fun Board(
    pieces: Map<Int, User>,
    selectedCell: Int = -1,
    turnUser: User = User.YOU,
    modifier: Modifier = Modifier,
    onTapCell: (cell: Int) -> Unit
) {
    val boardImage = remember {
        useResource("board.png", ::loadImageBitmap)
    }
    var tapPosition by remember { mutableStateOf(Offset(0f, 0f)) }
    var cursorPosition by remember { mutableStateOf(Offset(0f, 0f)) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
            .clipToBounds()
            .pointerMoveFilter(
                onMove = {
                    cursorPosition = it
                    true
                }
            )
            .pointerInput(Unit) {
                detectTapGestures {
                    tapPosition = it
                }
            }
    ) {
        drawIntoCanvas {
            val minDim = min(size.width, size.height)
            val maxDim = max(size.width, size.height)
            val boardOffset = when (size.width > size.height) {
                true -> Offset((maxDim - minDim) / 2.0f, 0f)
                false -> Offset(0f, (maxDim - minDim) / 2f)
            }
            val boardSize = Offset(minDim, minDim)

            drawImage(
                boardImage,
                dstOffset = IntOffset(boardOffset.x.toInt(), boardOffset.y.toInt()),
                dstSize = IntSize(boardSize.x.toInt(), boardSize.y.toInt())
            )

            val firstPositionOffset = (boardSize * .22f) + boardOffset
            val cellRadius = boardSize.x * .03f
            val cellSpacing = boardSize * .112f

            if (selectedCell >= 0) {
                println("selectedCell: %d, cursorPosition: %s".format(selectedCell, cursorPosition))
                drawCircle(color = toColor(turnUser), cellRadius, center = cursorPosition)
            }

            for (i in 0 until 36) {
                val piece = pieces[i]
                val cell = Offset((i % 6).toFloat(), (i / 6).toFloat())
                val centerPosition = firstPositionOffset + Offset(cell.x * cellSpacing.x, cell.y * cellSpacing.y)
                if (isInside(tapPosition, centerPosition, cellRadius)) {
                    tapPosition = Offset(0f, 0f)
                    onTapCell(i)
                }

                if (piece != null && i != selectedCell)
                    drawCircle(color = toColor(piece), radius = cellRadius, center = centerPosition)
            }
        }
    }
}

