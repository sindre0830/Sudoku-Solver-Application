package com.example.sudokusolver

import android.os.Bundle
import android.util.Log
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
import com.example.sudokusolver.ui.theme.SudokuSolverTheme
import com.example.sudokusolver.ui.theme.Teal200

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val verticalLength = 9
        setContent {
            val sudokuBoard: SnapshotStateList<SudokuBoardItem> =
                remember { setupBoard(mockBoard(9)) }
            val (indexedClicked, setIndexedClicked) = remember { mutableStateOf(0) }

            fun handleItemClicked(index: Int) {
                if (index in 0 until sudokuBoard.size) {

                    // reset last clicked
                    sudokuBoard[indexedClicked] =
                        sudokuBoard[indexedClicked].copy(backgroundColor = Color.White)
                    setIndexedClicked(index)

                    // update new clicked
                    sudokuBoard[index] =
                        sudokuBoard[index].copy(backgroundColor = Teal200)
                }
            }



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
                        Header()
                        SudokuBoard(
                            items = sudokuBoard,
                            verticalLength = verticalLength,
                            onItemClick = { index: Int -> handleItemClicked(index) }
                        )
                        ActionMenu(handleActionMenuItems(sudokuBoard))
                        BottomNumbers(handleClick = {})//TODO: Handle clicks
                        Spacer(modifier = Modifier.padding(bottom = 10.dp))
                    }
                }
            }
        }
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

