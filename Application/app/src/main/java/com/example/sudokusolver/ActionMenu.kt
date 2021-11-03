package com.example.sudokusolver

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sudokusolver.ui.theme.ColorBoxSelected

data class ActionMenuItem(
    val icon: ImageVector,
    val contentDescriptionResourceId: Int,
    val handleClick: () -> Unit
)

data class HistoryItem(
    val newValue: Int,
    val oldValue: Int,
    val sudokuItem: Int,
)

@Composable
fun ActionMenu(actionMenuItems: List<ActionMenuItem>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        actionMenuItems.forEach { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { item.handleClick() }
            ) {
                Icon(
                    item.icon,
                    contentDescription = stringResource(item.contentDescriptionResourceId),
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = stringResource(item.contentDescriptionResourceId),
                    style = TextStyle(fontSize = 12.sp)
                )

            }
        }
    }
}


fun handleActionMenuItems(
    sudokuItemClicked: Int,
    sudokuBoard: SnapshotStateList<SudokuBoardItem>,
    history: SnapshotStateList<HistoryItem>,
    startImageLoadingActivity: () -> Unit
): List<ActionMenuItem> {
    // TODO: Review icons used. Finding good icons is difficult...
    // See options: https://fonts.google.com/icons?selected=Material+Icons&icon.query=delete
    // We can also import external icons if we do not find anything we are happy with.
    return listOf(
        ActionMenuItem(
            icon = Icons.Rounded.Clear,
            contentDescriptionResourceId = R.string.action_menu_icon_description_clear,
            handleClick = { clearSudokuBoard(sudokuBoard) },
        ),
        ActionMenuItem(
            icon = Icons.Rounded.Undo,
            contentDescriptionResourceId = R.string.action_menu_icon_description_remove,
            handleClick = {
                undoOperation(sudokuBoard, history)
            },
        ),
        ActionMenuItem(
            icon = Icons.Rounded.Delete,
            contentDescriptionResourceId = R.string.action_menu_icon_description_delete,
            handleClick = {
                deleteSudokuItem(sudokuBoard, sudokuItemClicked)
            },
        ),
        ActionMenuItem(
            icon = Icons.Rounded.FileDownload,
            contentDescriptionResourceId = R.string.action_menu_icon_description_import,
            handleClick = { startImageLoadingActivity() },
        ),
        ActionMenuItem(
            icon = Icons.Rounded.Calculate,
            contentDescriptionResourceId = R.string.action_menu_icon_description_solve,
            handleClick = {}

        )
    )
}

fun deleteSudokuItem(sudokuBoard: SnapshotStateList<SudokuBoardItem>, sudokuItemClicked: Int) {
    mutateBoard(
        index = sudokuItemClicked,
        number = 0,
        board = sudokuBoard
    )
}

fun clearSudokuBoard(sudokuBoard: SnapshotStateList<SudokuBoardItem>) {
    for (i in 0 until sudokuBoard.size) {
        mutateBoard(
            index = i,
            number = 0,
            backgroundColor = Color.White,
            board = sudokuBoard
        )
    }
}

fun undoOperation(
    sudokuBoard: SnapshotStateList<SudokuBoardItem>,
    history: SnapshotStateList<HistoryItem>
) {
    if (history.isNotEmpty()) {
        val historyItem = history[history.lastIndex]
        history.remove(historyItem)
        mutateBoard(
            index = historyItem.sudokuItem,
            number = historyItem.oldValue,
            backgroundColor = Color.White,
            board = sudokuBoard
        )

        // Only move color box selected if there are more history entries
        if (history.isNotEmpty()) {
            mutateBoard(
                index = history[history.lastIndex].sudokuItem,
                backgroundColor = ColorBoxSelected,
                board = sudokuBoard
            )
        }
    }
}
