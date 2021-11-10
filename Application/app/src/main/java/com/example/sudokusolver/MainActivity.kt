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

        // works but unbearably slowx3 - 17 filled squares
        // 2:07
        val hardestBoard= arrayOf(
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

        // works but unbearably slow - 17 filled squares
        // 1 min
        val hardBoard = arrayOf(
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

        // 21 numbers - "impossible" on website
        // 1 sec
        val mediumHard = arrayOf(
            arrayOf(0,3,0,0,0,0,9,0,0),
            arrayOf(0,0,6,0,0,0,0,0,0),
            arrayOf(0,0,0,2,4,1,0,3,0),
            arrayOf(0,0,0,9,0,0,7,0,0),
            arrayOf(0,0,0,0,0,2,0,0,4),
            arrayOf(0,8,0,0,7,0,0,2,0),
            arrayOf(8,5,0,0,0,0,0,0,0),
            arrayOf(0,9,0,7,0,4,0,0,0),
            arrayOf(0,0,0,0,0,6,0,0,1)
        )

        // works but slow at 6s - 22 filled squares
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
        // 28 filled squares -
        val mediumEasy = arrayOf(
            arrayOf(0,0,8,0,0,6,0,7,0),
            arrayOf(4,0,0,0,0,0,0,5,3),
            arrayOf(0,0,5,4,0,0,0,0,0),
            arrayOf(6,0,7,9,0,5,3,0,1),
            arrayOf(9,8,0,0,0,0,0,6,5),
            arrayOf(0,0,0,7,0,1,0,0,0),
            arrayOf(0,0,0,0,0,2,1,0,0),
            arrayOf(1,3,0,0,0,0,0,0,8),
            arrayOf(0,9,0,1,0,0,4,0,0)
        )

        // works - 36 filled squares
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
        //val test = dumbVer
        //test.fill()
        // threading - fixes freeze at least
        Thread {
            val result = SudokuSolver.solve(mediumHard)
            SudokuSolver.printBoard(result.first)

            Log.i("Solved: ", result.second.toString())
        }.start()
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