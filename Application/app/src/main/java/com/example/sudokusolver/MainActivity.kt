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
import com.example.sudokusolver.dumbVer.removeCol
import com.example.sudokusolver.dumbVer.removeRow
import com.example.sudokusolver.dumbVer.removeUsedValues
import com.example.sudokusolver.dumbVer.solve
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

        val template = arrayOf(
            arrayOf(0,0,0,0,0,0,0,0,0),
            arrayOf(0,0,0,0,0,0,0,0,0),
            arrayOf(0,0,0,0,0,0,0,0,0),
            arrayOf(0,0,0,0,0,0,0,0,0),
            arrayOf(0,0,0,0,0,0,0,0,0),
            arrayOf(0,0,0,0,0,0,0,0,0),
            arrayOf(0,0,0,0,0,0,0,0,0),
            arrayOf(0,0,0,0,0,0,0,0,0),
            arrayOf(0,0,0,0,0,0,0,0,0)
        )


        val alsoHardBoard = arrayOf(
            arrayOf(0,0,0,8,0,1,0,0,0),
            arrayOf(0,0,0,0,0,0,4,3,0),
            arrayOf(5,0,0,0,0,0,0,0,0),
            arrayOf(0,0,0,0,7,0,8,0,0),
            arrayOf(0,0,0,0,0,0,1,0,0),
            arrayOf(0,2,0,0,3,0,0,0,0),
            arrayOf(6,0,0,0,0,0,0,7,5),
            arrayOf(0,0,3,4,0,0,0,0,0),
            arrayOf(0,0,0,2,0,0,6,0,0)
        )

        val hardBoard= arrayOf(
            arrayOf(0,0,0,7,0,0,0,0,0),
            arrayOf(1,0,0,0,0,0,0,0,0),
            arrayOf(0,0,0,4,3,0,2,0,0),
            arrayOf(0,0,0,0,0,0,0,0,6),
            arrayOf(0,0,0,5,0,9,0,0,0),
            arrayOf(0,0,0,0,0,0,4,1,8),
            arrayOf(0,0,0,0,8,1,0,0,0),
            arrayOf(0,0,2,0,0,0,0,5,0),
            arrayOf(0,4,0,0,0,0,3,0,0)
        )

        val mediumBoard = arrayOf(
            arrayOf(0,0,0,6,0,0,4,0,0),
            arrayOf(7,0,0,0,0,3,6,0,0),
            arrayOf(0,0,0,0,9,1,0,8,0),
            arrayOf(0,0,0,0,0,0,0,0,0),
            arrayOf(0,5,0,1,8,0,0,0,3),
            arrayOf(0,0,0,3,0,6,0,4,5),
            arrayOf(0,4,0,2,0,0,0,6,0),
            arrayOf(9,0,3,0,0,0,0,0,0),
            arrayOf(0,2,0,0,0,0,1,0,0)
        )

        // works - final square 9
        val easyBoard = arrayOf(
            arrayOf(0,0,0,2,6,0,7,0,1),
            arrayOf(6,8,0,0,7,0,0,9,0),
            arrayOf(1,9,0,0,0,4,5,0,0),
            arrayOf(8,2,0,1,0,0,0,4,0),
            arrayOf(0,0,4,6,0,2,9,0,0),
            arrayOf(0,5,0,0,0,3,0,2,8),
            arrayOf(0,0,9,3,0,0,0,7,4),
            arrayOf(0,4,0,0,5,0,0,3,6),
            arrayOf(7,0,3,0,1,8,0,0,0)
            )
        val test = dumbVer
        //test.fill()
        val result = solve(0, alsoHardBoard)
        for (i in result.first.indices) {
            Log.i("Board: ", result.first[i].contentToString())
        }
        Log.i("Solved: ", result.second.toString())
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