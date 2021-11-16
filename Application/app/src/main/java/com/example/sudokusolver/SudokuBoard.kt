package com.example.sudokusolver

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

data class SudokuBoardItem(
    val number: Int,
    val backgroundColor: Color = Color.White
)

fun mockBoard(verticalLength: Int): MutableList<Int> {
    val board: MutableList<Int> = mutableListOf()
    for (i in 0 until verticalLength * verticalLength) {
        board.add(i % 10)
    }
    return board
}

@Composable
fun SudokuBoardUI(
    sudokuBoardItems: List<SudokuBoardItem>,
    verticalLength: Int,
    onItemClick: (index: Int) -> Unit,
) {
    validateBoard(sudokuBoardItems.map { it.number }, verticalLength)

    var currentItem = 0

    Column {
        for (row in verticalLength downTo 1) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (rowItem in 0 until verticalLength) {
                    SudokuBoxItem(
                        index = currentItem,
                        item = sudokuBoardItems[currentItem],
                        onItemClick = onItemClick
                    )
                    currentItem++
                }
            }
        }
    }
}

fun validateBoard(items: List<Int>, verticalLength: Int) {
    if (verticalLength == 0) {
        throw Error("board length cannot be 0")
    }

    if (!isAxesSameNumber(items, verticalLength)) {
        throw Error("The board can only be 9x9 or 6x6")
    }
    if (!isValidSudokuNumbers(items)) {
        throw Error("accepted numbers are 0-9")
    }
}

// The board can only be 9x9 or 6x6.
fun isAxesSameNumber(items: List<Int>, verticalLength: Int) =
    items.size / verticalLength == verticalLength

// 0-9 is the only numbers accepted where 0 is empty/blank
fun isValidSudokuNumbers(items: List<Int>): Boolean = items.all { it in 0..9 }

@Composable
fun RowScope.SudokuBoxItem(
    index: Int,
    item: SudokuBoardItem,
    onItemClick: (index: Int) -> Unit
) {
    Box(
        modifier = Modifier
            .border(
                border = BorderStroke(1.dp, MaterialTheme.colors.onBackground),
                shape = RectangleShape
            )
            .fillMaxWidth()
            .aspectRatio(1f)
            .weight(1f)
            .clickable { onItemClick(index) }
            .background(if (item.backgroundColor == Color.White) MaterialTheme.colors.background else item.backgroundColor)
    ) {
        Text(
            text = if (item.number == 0) "" else item.number.toString(),
            style = TextStyle(color = MaterialTheme.colors.onBackground),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
