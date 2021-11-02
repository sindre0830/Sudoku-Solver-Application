package com.example.sudokusolver

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp


fun mockBoard(verticalLength: Int): List<Int> {
    val board: MutableList<Int> = mutableListOf()
    for (i in 0 until verticalLength * verticalLength) {
        board.add(i % 10)
    }
    return board
}

@Composable
fun SudokuBoard(items: List<Int>, verticalLength: Int) {
    validateBoard(items, verticalLength)
    var currentItem = 0

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column {
            for (row in verticalLength downTo 1) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (rowItem in 0 until verticalLength) {
                        SudokuBoxItem(items[currentItem++])
                    }
                }
            }
        }
    }
}


// TODO: figure out a nice way to handle error and display them to user
fun validateBoard(items: List<Int>, verticalLength: Int) {
    if (verticalLength == 0 || !isAxesSameNumber(items, verticalLength)) {
        throw Error("board length cannot be 0, and it has to be 9x9 etc")
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
fun RowScope.SudokuBoxItem(number: Int) {
    Box(
        modifier = Modifier
            .border(
                // TODO: fix color on different modes https://developer.android.com/jetpack/compose/themes/material
                border = BorderStroke(1.dp, Color.Black),
                shape = RectangleShape
            )
            .fillMaxWidth()
            .height(60.dp)
            .weight(1f)
    ) {
        Text(
            text = if (number == 0) "" else number.toString(),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}