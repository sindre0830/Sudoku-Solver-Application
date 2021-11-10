package com.example.sudokusolver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import com.example.sudokusolver.ui.theme.SudokuSolverTheme
import java.io.Serializable

class HistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val history =
                (intent.getSerializableExtra(SUDOKU_BOARD_HISTORY_KEY) as List<*>).map { it as PreviousGamesHistoryItems }

            SudokuSolverTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    history.forEach {
                        Text(text = it.date)
                    }
                }
            }
        }
    }
}

data class PreviousGamesHistoryItems(val date: String, val board: List<Int>) : Serializable
