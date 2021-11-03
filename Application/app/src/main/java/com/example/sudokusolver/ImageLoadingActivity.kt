package com.example.sudokusolver

import android.graphics.drawable.Icon
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.sudokusolver.ui.theme.SudokuSolverTheme

class ImageLoadingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        data class ActivityButton(
            val icon: ImageVector,
            val descriptionResourceId: Int,
            val onClick: () -> Unit
        )

        val buttons = listOf(
            ActivityButton(
                Icons.Rounded.PhotoCamera,
                R.string.image_loading_activity_icon_description_camera,
                {/*TODO: Launch Camera*/ }
            ),
            ActivityButton(
                Icons.Rounded.Collections,
                R.string.image_loading_activity_icon_description_gallery,
                { /*TODO: Launch Gallery*/ },
            ),
            ActivityButton(
                Icons.Rounded.Casino,
                R.string.image_loading_activity_icon_description_random,
                { /*TODO: Load hardcoded/random board*/ }
            )

        )

        super.onCreate(savedInstanceState)
        setContent {
            SudokuSolverTheme {
                Surface(
                    color = MaterialTheme.colors.background,
                    modifier = Modifier.padding(10.dp)
                ) {
                    Header {
                        ArrowBackIcon(
                            modifier = Modifier.clickable { finish() }
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        buttons.forEach {
                            Button(
                                onClick = it.onClick,
                                modifier = Modifier.size(width = 200.dp, height = 60.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.width(200.dp)
                                ) {
                                    Icon(
                                        it.icon,
                                        contentDescription = stringResource(id = it.descriptionResourceId),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.padding(10.dp))
                                    Text(text = stringResource(id = it.descriptionResourceId))
                                }
                            }
                            Spacer(modifier = Modifier.padding(20.dp))
                        }
                    }
                }
            }
        }
    }
}
