package com.example.sudokusolver

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ActionMenuItem(
    val icon: ImageVector,
    val contentDescriptionResourceId: Int,
    val handleClick: () -> Unit
)

data class CurrentGameHistoryItem(
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
    board: List<SudokuBoardItem>,
    currentGameHistory: SnapshotStateList<CurrentGameHistoryItem>,
    startImageLoadingActivity: () -> Unit,
    mutateBoard: mutateBoardFn,
    mutateBoardNumber: mutateBoardNumberFn,
    addSudokuBoardAsSolved: (List<Int>) -> Unit,
    context: Context
): List<ActionMenuItem> {
    return listOf(
        ActionMenuItem(
            icon = Icons.Rounded.Clear,
            contentDescriptionResourceId = R.string.action_menu_icon_description_clear,
            handleClick = { clearSudokuBoard(board.size, mutateBoard) },
        ),
        ActionMenuItem(
            icon = Icons.Rounded.Undo,
            contentDescriptionResourceId = R.string.action_menu_icon_description_remove,
            handleClick = {
                undoOperation(mutateBoardNumber, currentGameHistory)
            },
        ),
        ActionMenuItem(
            icon = Icons.Rounded.Delete,
            contentDescriptionResourceId = R.string.action_menu_icon_description_delete,
            handleClick = {
                deleteSudokuItem(sudokuItemClicked, mutateBoardNumber)
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
            handleClick = {
                val solved = SudokuSolver(context).fill(board.map { it.number }.toTypedArray())
                for (i in solved.first.indices) {
                    mutateBoardNumber(i, solved.first[i])
                }
                // Only add to history if solved
                if (solved.second != null) {
                    // error handle here
                } else {
                    addSudokuBoardAsSolved(board.map { it.number })
                }
            }

        )
    )
}

fun deleteSudokuItem(sudokuItemClicked: Int, mutateBoardNumber: mutateBoardNumberFn) {
    mutateBoardNumber(
        sudokuItemClicked,
        0,
    )
}

fun clearSudokuBoard(boardSize: Int, mutateBoard: mutateBoardFn) {
    for (i in 0 until boardSize) {
        mutateBoard(
            i,
            Color.White,
            0,
        )
    }
}

fun undoOperation(
    mutateBoardNumber: mutateBoardNumberFn,
    history: SnapshotStateList<CurrentGameHistoryItem>
) {
    if (history.isNotEmpty()) {
        val historyItem = history[history.lastIndex]
        history.remove(historyItem)
        mutateBoardNumber(
            historyItem.sudokuItem,
            historyItem.oldValue,
        )

        // we also have to remove the history item after the mutation above
        history.remove(history[history.lastIndex])
    }
}
