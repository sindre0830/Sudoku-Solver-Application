package com.example.sudokusolver

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.sudokusolver.ui.theme.SudokuSolverTheme
import java.util.ArrayList

class SampleImagesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val imageResourceIds = listOf(
                R.drawable.sudokuboard1,
                R.drawable.sudokuboard2,
                R.drawable.sudokoboard3,
                R.drawable.sudokoboard4,
                R.drawable.sudokoboard5,
            )
            SudokuSolverTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    color = MaterialTheme.colors.background
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(imageResourceIds) { id ->
                            DisplayResourceImage(id)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DisplayResourceImage(resourceId: Int) {
    val context = LocalContext.current
    val bitmap = ImageBitmap.imageResource(id = resourceId)
    Image(
        painter = painterResource(id = resourceId),
        contentDescription = "sample image",
        modifier = Modifier
            .size(200.dp)
            .clickable {
                val recognizer = SudokuBoardRecognizer(context)
                recognizer.setImageFromBitmap(bitmap.asAndroidBitmap())
                val (predictionOutput, error) = recognizer.execute()

                Log.d("OpenCV", predictionOutput.toString())
                context.startActivity(
                    Intent(
                        context,
                        MainActivity::class.java
                    ).putIntegerArrayListExtra(
                        SUDOKU_BOARD_KEY, ArrayList(predictionOutput)
                    )
                )
            }
    )
}
