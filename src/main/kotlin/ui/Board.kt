package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import lib.Player
import lib.toColor
import lib.toOther
import org.jetbrains.skija.ColorFilter
import kotlin.math.max
import kotlin.math.min

private operator fun Offset.div(other: Offset): Offset {
    return Offset(x / other.x, y / other.y)
}

private operator fun Offset.times(other: Offset): Offset {
    return Offset(x * other.x, y * other.y)
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
    pieces: Map<Int, Player>,
    enabled: Boolean,
    turnPlayer: Player,
    selectedCell: Int = -1,
    adversaryMousePosition: Offset = Offset.Zero,
    modifier: Modifier = Modifier,
    onTapCell: (cell: Int) -> Unit,
    onCursorMove: (position: Offset) -> Unit
) {
    val boardImage = remember {
        useResource("board.png", ::loadImageBitmap)
    }
    var tapPosition by remember { mutableStateOf(Offset.Zero) }
    var cursorPosition by remember { mutableStateOf(Offset.Zero) }
    val alpha = when (enabled) {
        true -> 1.0f
        false -> .5f
    }

    LaunchedEffect(cursorPosition.toString()) {
        onCursorMove(cursorPosition)
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
            .graphicsLayer(alpha = alpha)
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

            if (selectedCell >= 0 && enabled) {
                drawCircle(color = turnPlayer.toColor(), cellRadius, center = clamp(cursorPosition, size))
            }

            if (selectedCell >= 0 && !enabled) {
                drawCircle(color = turnPlayer.toColor(), cellRadius, center = clamp(adversaryMousePosition, size))
            }

            for (i in 0 until 36) {
                val piece = pieces[i]
                val cell = Offset((i % 6).toFloat(), (i / 6).toFloat())
                val centerPosition = clamp(firstPositionOffset + Offset(cell.x * cellSpacing.x, cell.y * cellSpacing.y), size)
                if (enabled && isInside(tapPosition, centerPosition, cellRadius)) {
                    tapPosition = Offset.Zero
                    onTapCell(i)
                }

                if (piece != null && i != selectedCell)
                    drawCircle(color = piece.toColor(), radius = cellRadius, center = centerPosition)
            }
        }
    }
}

private fun clamp(cursorPosition: Offset, size: Size): Offset {
    return Offset(
        0f.coerceAtLeast(cursorPosition.x.coerceAtMost(size.width)),
        0f.coerceAtLeast(cursorPosition.y.coerceAtMost(size.height)),
    )
}


