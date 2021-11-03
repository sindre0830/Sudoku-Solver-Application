package com.example.sudokusolver

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.History
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Header(
    RightIcon: @Composable() () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = TextStyle(fontSize = 18.sp)
        )
        RightIcon()
    }
}

@Composable
fun HistoryIcon() {
    Icon(
        Icons.Rounded.History,
        contentDescription = stringResource(id = R.string.header_history_icon_description),
        modifier = Modifier.size(32.dp)
    )
}

@Composable
fun ArrowBackIcon(
    modifier: Modifier = Modifier
) {
    Icon(
        Icons.Rounded.ArrowBack,
        contentDescription = stringResource(id = R.string.header_arrow_back_icon_description),
        modifier = modifier.size(32.dp)
    )
}