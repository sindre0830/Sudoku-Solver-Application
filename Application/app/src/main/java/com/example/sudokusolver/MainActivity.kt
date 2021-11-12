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
        // Want to do our solves in a thread
        //Thread {
            //var test = SudokuSolver
            //test.fill(arrayOf(0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 7, 0, 0, 2, 0, 0, 8, 0, 3, 0, 0, 5, 0, 0, 8, 0, 0, 0, 5, 0, 0, 0, 2, 0, 4, 0, 9, 0, 3, 0, 9, 0, 0, 6, 0, 7, 0, 0, 2, 5, 0, 9, 0, 0, 0, 3, 0, 8, 0, 0, 3, 0, 0, 0, 9, 0, 0, 0, 7, 0, 9, 0, 4, 0, 5, 0))
            //var result = test.solve()
            //Log.i("Done: ", result.first.contentToString())
        //}.start()
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