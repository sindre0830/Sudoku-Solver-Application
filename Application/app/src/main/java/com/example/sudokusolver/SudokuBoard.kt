package com.example.sudokusolver

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Stores the given board and makes it for other code to interact with the board in safe manner
class SudokuBoard(
    private val sudokuBoardItems: SnapshotStateList<SudokuBoardItem>,
    val currentGameHistory: SnapshotStateList<CurrentGameHistoryItem>,
    private val dataStore: DataStore<Preferences>
) {

    // Is an immutable list which we can share with others.
    // This is done to prevent direct mutation of the board.
    // All mutation must go through the mutate function
    var items: List<SudokuBoardItem> = sudokuBoardItems

    fun mutate(
        index: Int,
        backgroundColor: Color? = null,
        number: Int? = null,
    ) {
        if (!isBoxWithinBoard(index, sudokuBoardItems.size)) return
        number?.let { if (!isValidSudokuNum(number)) return }

        updateCurrentGameHistory(index, number)
        mutateBoard(index, backgroundColor, number)
        CoroutineScope(Dispatchers.Main).launch {
            persistBoard()
        }
    }

    fun mutate(index: Int, backgroundColor: Color) = mutate(index, backgroundColor, null)
    fun mutate(index: Int, number: Int) = mutate(index, null, number)


    private fun updateCurrentGameHistory(
        index: Int,
        number: Int?,
    ) {
        number?.let {
            if (isNewNumber(sudokuBoardItems[index].number, number)) {
                currentGameHistory.add(
                    CurrentGameHistoryItem(
                        newValue = number,
                        oldValue = sudokuBoardItems[index].number,
                        sudokuItem = index
                    )
                )
            }
        }
    }

    // Mutates the board by creating a new SudokuBoardItem.
    // This is done to trigger a UI update. Updating a class member will not trigger a UI update
    private fun mutateBoard(
        index: Int,
        backgroundColor: Color?,
        number: Int?,
    ) {
        sudokuBoardItems[index] = sudokuBoardItems[index].copy(
            backgroundColor = backgroundColor ?: sudokuBoardItems[index].backgroundColor,
            number = number ?: sudokuBoardItems[index].number
        )
        items = sudokuBoardItems.toList()
    }

    private fun isNewNumber(old: Int, new: Int) = old != new
    private fun isBoxWithinBoard(n: Int, boardSize: Int) = n in 0 until boardSize
    private fun isValidSudokuNum(n: Int) = n in 0..9

    private suspend fun persistBoard() {
        dataStore.edit { pref ->
            pref[SUDOKU_BOARD] = sudokuBoardItems.map { it.number }.joinToString(separator = ",")
        }
    }
}