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

        // actual sudoku puzzle
        val newBoard = mutableListOf(   5,3,0,0,7,8,0,0,0,
                                        6,0,0,1,9,5,0,0,0,
                                        0,9,8,0,0,0,0,6,0,
                                        8,0,0,0,6,0,0,0,3,
                                        4,0,0,8,0,3,0,0,1,
                                        7,0,0,0,2,0,0,0,6,
                                        0,6,0,0,0,0,2,8,0,
                                        0,0,0,4,1,9,0,0,5,
                                        0,0,0,0,8,0,0,7,9,  )
        val test = dumbVer
        test.fill(newBoard)
        test.PrintBoard()
        /*
        val board = GridModel
        // works!
        board.fill(newBoard)
        board.PrintBoard()
        // not works!
        board.solver.solve()

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