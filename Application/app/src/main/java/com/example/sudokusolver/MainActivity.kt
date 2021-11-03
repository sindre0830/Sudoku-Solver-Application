package com.example.sudokusolver

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.sudokusolver.ui.theme.SudokuSolverTheme

class MainActivity : ComponentActivity( ) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SudokuSolverTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Greeting("Android")
                }
            }
        }

        val board = GridModel
        board.PrintBoard()
        /*
        // Generates boards!
        val board = Sudoku.Builder().setLevel(Level.MEDIUM).build()
        board.printGrid()
        // We need to change the sourcecode so we can pass the board into
        // the solver the way we want, but otherwise this works.
        // Cannot guarantee speed however!
        // Need to optimize/multithread...
        val solved = Solver.solvable(board.grid)
        if(solved.first) {
            Solver.printGrid(solved.second)
        } else {
            Log.d("UNSOLVABLE", "BOO")
        }
         */
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SudokuSolverTheme {
        Greeting("Android")
    }
}