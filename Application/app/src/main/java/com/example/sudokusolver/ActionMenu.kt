package com.example.sudokusolver

import android.content.Context
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
    sudokuBoard: SudokuBoard,
    currentGameHistory: SnapshotStateList<CurrentGameHistoryItem>,
    startImageLoadingActivity: () -> Unit,
    context: Context,
    addSudokuBoardAsSolved: (List<Int>) -> Unit,
    displayError: (msg: String) -> Unit
): List<ActionMenuItem> {
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
                undoOperation(sudokuBoard, currentGameHistory)
            },
        ),
        ActionMenuItem(
            icon = Icons.Rounded.Delete,
            contentDescriptionResourceId = R.string.action_menu_icon_description_delete,
            handleClick = {
                deleteSudokuItem(sudokuItemClicked, sudokuBoard)
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
                val (solvedBoard, error) = SudokuSolver(context).init(
                    sudokuBoard.items.map { it.number }.toTypedArray()
                )
                // Only add to history if solved
                if (error != null) {
                    displayError(error)
                } else {
                    for (i in solvedBoard.indices) {
                        sudokuBoard.mutate(i, solvedBoard[i])
                    }
                    addSudokuBoardAsSolved(sudokuBoard.items.map { it.number })
                }
            }
        )
    )
}

fun deleteSudokuItem(sudokuItemClicked: Int, board: SudokuBoard) {
    board.mutate(
        sudokuItemClicked,
        0,
    )
}

fun clearSudokuBoard(board: SudokuBoard) {
    for (i in board.items.indices) {
        board.mutate(
            i,
            Color.White,
            0,
        )
    }
}

fun undoOperation(
    board: SudokuBoard,
    history: SnapshotStateList<CurrentGameHistoryItem>
) {
    if (history.isNotEmpty()) {
        val historyItem = history[history.lastIndex]
        history.remove(historyItem)
        board.mutate(
            historyItem.sudokuItem,
            historyItem.oldValue,
        )

        // we also have to remove the history item after the mutation above
        history.remove(history[history.lastIndex])
    }
}
