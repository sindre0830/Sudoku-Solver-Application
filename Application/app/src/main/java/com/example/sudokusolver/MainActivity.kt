package com.example.sudokusolver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sudokusolver.ui.theme.SudokuSolverTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val verticalLength = 6
        val board = mockBoard(verticalLength)
        setContent {
            SudokuSolverTheme {
                // A surface container using the 'background' color from the theme
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
                        SudokuBoard(items = board, verticalLength = verticalLength)
                        ActionMenu()
                        BottomNumbers(handleClick = {})//TODO: Handle clicks
                        Spacer(modifier = Modifier.padding(bottom = 10.dp))
                    }
                }
            }
        }
    }
}
