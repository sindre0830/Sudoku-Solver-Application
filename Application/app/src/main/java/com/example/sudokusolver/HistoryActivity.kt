package com.example.sudokusolver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sudokusolver.ui.theme.SudokuSolverTheme
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date

class HistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            @Suppress("UNCHECKED_CAST")
            val history =
                intent.getSerializableExtra(SUDOKU_BOARD_HISTORY_KEY) as? List<PreviousGamesHistoryItem>
                    ?: listOf()

            SudokuSolverTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    color = MaterialTheme.colors.background,
                    modifier = Modifier.padding(15.dp)
                ) {
                    when (history.size) {
                        0 -> Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "Empty game history \uD83D\uDE22",
                                fontSize = 24.sp
                            )
                        }
                        else -> {
                            LazyColumn {
                                items(history.reversed()) { historyItem ->
                                    HistoryActivityItem(historyItem)
                                    Divider(
                                        color = Color.Gray,
                                        thickness = 1.dp,
                                        modifier = Modifier
                                            .padding(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class PreviousGamesHistoryItem(val date: Date, val board: List<Int>) : Serializable

@Composable
fun HistoryActivityItem(historyItem: PreviousGamesHistoryItem) {
    Row(

        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier
                .size(150.dp),
        ) {
            SudokuBoard(
                items = historyItem.board.map { SudokuBoardItem(it) },
                verticalLength = 9,
                onItemClick = {}
            )
        }

        Spacer(modifier = Modifier.padding(20.dp))
        Text(text = SimpleDateFormat("M/dd/yyyy HH:mm:ss").format(historyItem.date))
    }
}
