package com.example.sudokusolver

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
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


                recognizer.execute()
                val predictionOutput = recognizer.predictionOutput

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
