package com.example.sudokusolver

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.util.*
import kotlin.collections.ArrayList

const val SUDOKU_BOARD_KEY = "sudoku_board"
const val SUDOKU_BOARD_HISTORY_KEY = "sudoku_board_history"
const val ERROR_EXTRA = "ERROR"

// adding store here to ensure it is a singleton
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SUDOKU_BOARD_KEY)

// stringPreferences are used for storing the board
val SUDOKU_BOARD = stringPreferencesKey(SUDOKU_BOARD_KEY)
private val SUDOKU_BOARD_HISTORY = stringPreferencesKey(SUDOKU_BOARD_HISTORY_KEY)
typealias mutateBoardColorFn = (index: Int, backgroundColor: Color) -> Unit

class MainActivity : ComponentActivity() {

    companion object {
        lateinit var sudokuBoard: SudokuBoard
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val verticalLength = 9

        setContent {
            val (error, setError) = remember { mutableStateOf(intent.getStringExtra(ERROR_EXTRA)) }
            val (sudokuBoxClicked, setSudokuBoxClicked) = remember { mutableStateOf(0) }
            val boardNumbers: List<Int> = runBlocking {
                intent.getIntegerArrayListExtra(SUDOKU_BOARD_KEY)?.toList()
                    ?: loadBoard()
                    ?: mockBoard(9)
            }
            sudokuBoard = SudokuBoard(
                sudokuBoardItems = remember { setupBoard(boardNumbers) },
                currentGameHistory = remember { mutableStateListOf() },
                dataStore = dataStore
            )

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
                        SudokuBoardUI(
                            sudokuBoardItems = sudokuBoard.items,
                            verticalLength = verticalLength,
                            onItemClick = { index: Int ->
                                updateBackgroundColor(
                                    newSudokuBox = index,
                                    currentSudokuBox = sudokuBoxClicked,
                                    setSudokuBox = setSudokuBoxClicked,
                                    mutateBoardColor = { i, backgroundColor ->
                                        sudokuBoard.mutate(
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
                                sudokuBoard = sudokuBoard,
                                currentGameHistory = sudokuBoard.currentGameHistory,
                                startImageLoadingActivity = {
                                    Intent(
                                        applicationContext,
                                        ImageLoadingActivity::class.java
                                    ).also {
                                        startActivity(it)
                                    }
                                },
                                addSudokuBoardAsSolved = { solvedBoardNumbers ->
                                    lifecycleScope.launch {
                                        saveBoardToHistory(
                                            solvedBoardNumbers
                                        )
                                    }
                                },
                                context = LocalContext.current,
                                displayError = { msg ->
                                    setError(msg)
                                }
                            )
                        )
                        BottomNumbers(handleClick = { numberClicked ->
                            sudokuBoard.mutate(
                                sudokuBoxClicked,
                                numberClicked
                            )
                        })
                        Spacer(modifier = Modifier.padding(bottom = 10.dp))
                    }

                    if (error != null) {
                        DisplayError(error) {
                            setError(null)
                        }
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

    // Loads the last played board
    private suspend fun loadBoard(): List<Int>? {
        val pref = dataStore.data.first()
        return pref[SUDOKU_BOARD]?.split(",")?.map { it.toInt() }
    }

    // Saves the board history as a serialized json string
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

    // Loads the board history and deserialize the json string
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

@Composable
fun DisplayError(msg: String, removeErrorMsg: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Transparent)
            .clickable {
                removeErrorMsg()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(75.dp)
                .padding(10.dp)
                .background(color = Color(color = 0xFF8B0000)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = msg,
                style = TextStyle(color = Color.White, fontSize = 16.sp),
            )
        }
    }
}

// Converts the board item numbers to a list of SudokuBoardItem
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
