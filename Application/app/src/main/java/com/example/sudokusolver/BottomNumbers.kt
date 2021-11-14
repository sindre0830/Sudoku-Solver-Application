package com.example.sudokusolver

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BottomNumbers(handleClick: (number: Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (i in 1..9) {
            ClickableNumber(number = i, handleClick = handleClick)
        }
    }
}

@Composable
fun RowScope.ClickableNumber(number: Int, handleClick: (number: Int) -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clickable { handleClick(number) }
            .weight(1f)
            .padding(2.dp),
    ) {
        Text(
            text = number.toString(),
        )
    }
}
