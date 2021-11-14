package com.example.sudokusolver

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.example.sudokusolver.ui.theme.ColorBoxSelected
import com.example.sudokusolver.ui.theme.SudokuSolverTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Date
import kotlin.collections.ArrayList

const val SUDOKU_BOARD_KEY = "sudoku_board"
const val SUDOKU_BOARD_HISTORY_KEY = "sudoku_board_history"

// adding store here to ensure it is a singleton
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SUDOKU_BOARD_KEY)

typealias mutateBoardFn = (index: Int, backgroundColor: Color, number: Int) -> Unit
typealias mutateBoardColorFn = (index: Int, backgroundColor: Color) -> Unit
typealias mutateBoardNumberFn = (index: Int, number: Int) -> Unit

class MainActivity : ComponentActivity() {
    private val SUDOKU_BOARD = stringPreferencesKey(SUDOKU_BOARD_KEY)
    private val SUDOKU_BOARD_HISTORY = stringPreferencesKey(SUDOKU_BOARD_HISTORY_KEY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val verticalLength = 9

        setContent {
            val board: List<Int> = runBlocking {
                intent.getIntegerArrayListExtra(SUDOKU_BOARD_KEY)?.toList()
                    ?: loadBoard()
                    ?: mockBoard(9)
            }

            val sudokuBoard: SnapshotStateList<SudokuBoardItem> =
                remember { setupBoard(board) }
            val (sudokuBoxClicked, setSudokuBoxClicked) = remember { mutableStateOf(0) }
            val currentGameHistory: SnapshotStateList<CurrentGameHistoryItem> =
                remember { mutableStateListOf() }

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
                        currentGameHistory.add(
                            CurrentGameHistoryItem(
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
                            HistoryIcon(
                                modifier = Modifier.clickable {
                                    Intent(
                                        applicationContext,
                                        HistoryActivity::class.java
                                    ).apply {
                                        putExtra(
                                            SUDOKU_BOARD_HISTORY_KEY,
                                            ArrayList(runBlocking { loadBoardHistory() })
                                        )
                                    }.also {
                                        startActivity(it)
                                    }
                                }
                            )
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
                                board = sudokuBoard,
                                currentGameHistory = currentGameHistory,
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
                                addSudokuBoardAsSolved = { solvedBoardNumbers ->
                                    lifecycleScope.launch { saveBoardToHistory(solvedBoardNumbers) }
                                }
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
        // Want to do our solves in a thread
        // Thread {
        // var test = SudokuSolver
        // test.fill(arrayOf(0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 7, 0, 0, 2, 0, 0, 8, 0, 3, 0, 0, 5, 0, 0, 8, 0, 0, 0, 5, 0, 0, 0, 2, 0, 4, 0, 9, 0, 3, 0, 9, 0, 0, 6, 0, 7, 0, 0, 2, 5, 0, 9, 0, 0, 0, 3, 0, 8, 0, 0, 3, 0, 0, 0, 9, 0, 0, 0, 7, 0, 9, 0, 4, 0, 5, 0))
        // var result = test.solve()
        // Log.i("Done: ", result.first.contentToString())
        // }.start()
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

    private suspend fun saveBoardToHistory(board: List<Int>) {
        dataStore.edit { pref ->
            val historyEntries = loadBoardHistory()
            historyEntries.add(
                PreviousGamesHistoryItem(
                    Date(),
                    board
                )
            )
            pref[SUDOKU_BOARD_HISTORY] = Gson().toJson(historyEntries)
        }
    }

    private suspend fun loadBoardHistory(): MutableList<PreviousGamesHistoryItem> {
        val pref = dataStore.data.first()
        val historyStr = pref[SUDOKU_BOARD_HISTORY] ?: return mutableListOf()
        val gson = Gson()
        val historyEntriesType =
            object : TypeToken<MutableList<PreviousGamesHistoryItem>>() {}.type
        return gson.fromJson(
            historyStr,
            historyEntriesType
        )
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
