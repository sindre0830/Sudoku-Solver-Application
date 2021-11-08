package com.example.sudokusolver

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.example.sudokusolver.ui.theme.ColorBoxSelected
import com.example.sudokusolver.ui.theme.SudokuSolverTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

const val SUDOKU_BOARD_KEY = "sudoku_board"
typealias mutateBoardFn = (index: Int, backgroundColor: Color, number: Int) -> Unit
typealias mutateBoardColorFn = (index: Int, backgroundColor: Color) -> Unit
typealias mutateBoardNumberFn = (index: Int, number: Int) -> Unit

class MainActivity : ComponentActivity() {
    private val SUDOKU_BOARD = stringPreferencesKey(SUDOKU_BOARD_KEY)
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SUDOKU_BOARD_KEY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val verticalLength = 9

        setContent {
            val board: List<Int> = runBlocking {
                intent.getIntArrayExtra(SUDOKU_BOARD_KEY)?.toList() ?: loadBoard() ?: mockBoard(9)
            }

            val sudokuBoard: SnapshotStateList<SudokuBoardItem> =
                remember { setupBoard(board) }
            val (sudokuBoxClicked, setSudokuBoxClicked) = remember { mutableStateOf(0) }
            val history: SnapshotStateList<HistoryItem> = remember { mutableStateListOf() }
            fun isNewNumber(old: Int, new: Int) = old != new

            // mutateBoard mutates a sudoku item and allows for optional arguments
            fun mutateBoard(
                index: Int,
                backgroundColor: Color? = null,
                number: Int? = null,
            ) {
                if (!isBoxWithinBoard(index, sudokuBoard.size)) return
                number?.let { if (!isValidSudokuNum(number)) return }

                number?.let {
                    if (isNewNumber(sudokuBoard[index].number, number)) {
                        history.add(
                            HistoryItem(
                                newValue = number,
                                oldValue = sudokuBoard[index].number,
                                sudokuItem = index
                            )
                        )
                    }
                }

                sudokuBoard[index] = sudokuBoard[index].copy(
                    backgroundColor = backgroundColor ?: sudokuBoard[index].backgroundColor,
                    number = number ?: sudokuBoard[index].number
                )

                lifecycleScope.launch {
                    persistBoard(sudokuBoard.map { it.number })
                }
            }

            fun mutateBoardColor(index: Int, backgroundColor: Color) =
                mutateBoard(index, backgroundColor, null)

            fun mutateBoardNumber(index: Int, number: Int) =
                mutateBoard(index, null, number)

            SudokuSolverTheme {
                Surface(
                    color = MaterialTheme.colors.background,
                ) {
                    Column(
                        modifier = Modifier
                            .padding(5.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Header {
                            HistoryIcon()
                        }
                        SudokuBoard(
                            items = sudokuBoard,
                            verticalLength = verticalLength,
                            onItemClick = { index: Int ->
                                updateBackgroundColor(
                                    newSudokuBox = index,
                                    currentSudokuBox = sudokuBoxClicked,
                                    setSudokuBox = setSudokuBoxClicked,
                                    mutateBoardColor = { i, backgroundColor ->
                                        mutateBoardColor(
                                            i,
                                            backgroundColor
                                        )
                                    },
                                )
                            }
                        )
                        ActionMenu(
                            handleActionMenuItems(
                                sudokuItemClicked = sudokuBoxClicked,
                                boardSize = sudokuBoard.size,
                                history = history,
                                startImageLoadingActivity = {
                                    Intent(
                                        applicationContext,
                                        ImageLoadingActivity::class.java
                                    ).also {
                                        startActivity(it)
                                    }
                                },
                                mutateBoard = { i, bg, num -> mutateBoard(i, bg, num) },
                                mutateBoardNumber = { i, num -> mutateBoardNumber(i, num) },
                            )
                        )
                        BottomNumbers(handleClick = { numberClicked ->
                            mutateBoardNumber(
                                sudokuBoxClicked,
                                numberClicked
                            )
                        })
                        Spacer(modifier = Modifier.padding(bottom = 10.dp))
                    }
                }
            }
        }
    }

    private suspend fun persistBoard(board: List<Int>) {
        dataStore.edit { pref ->
            pref[SUDOKU_BOARD] = board.joinToString(separator = ",")
        }
    }

    private suspend fun loadBoard(): List<Int>? {
        val pref = dataStore.data.first()
        return pref[SUDOKU_BOARD]?.split(",")?.map { it.toInt() }
    }
}

fun setupBoard(items: List<Int>): SnapshotStateList<SudokuBoardItem> {
    val list = mutableStateListOf<SudokuBoardItem>()
    items.forEach {
        list.add(
            SudokuBoardItem(
                number = it,
            )
        )
    }
    return list
}


fun updateBackgroundColor(
    newSudokuBox: Int,
    currentSudokuBox: Int,
    setSudokuBox: (Int) -> Unit,
    mutateBoardColor: mutateBoardColorFn,
) {
    // reset old box
    mutateBoardColor(
        currentSudokuBox,
        Color.White,
    )

    setSudokuBox(newSudokuBox)

    // update new clicked
    mutateBoardColor(
        newSudokuBox,
        ColorBoxSelected,
    )

}

fun isBoxWithinBoard(n: Int, boardSize: Int) = n in 0 until boardSize


fun isValidSudokuNum(n: Int) = n in 0..9


