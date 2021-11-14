package com.example.sudokusolver

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.sudokusolver.ui.theme.SudokuSolverTheme
import kotlinx.coroutines.launch
import java.util.ArrayList

class ImageLoadingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scaffoldState = rememberScaffoldState()
            val snackbarCoroutineScope = rememberCoroutineScope()
            SudokuSolverTheme {
                Surface(
                    color = MaterialTheme.colors.background,
                    modifier = Modifier.padding(10.dp)
                ) {
                    Scaffold(
                        scaffoldState = scaffoldState,
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
                            LaunchCameraBtn(
                                displaySnackbar = { msg ->
                                    snackbarCoroutineScope.launch {
                                        scaffoldState.snackbarHostState.showSnackbar(
                                            message = msg,
                                        )
                                    }
                                }
                            )
                            LoadImageFromGalleryBtn()

                            SampleBoardsBtn()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SampleBoardsBtn() {
    val context = LocalContext.current

    ButtonWithIcon(
        onClick = {
            context.startActivity(Intent(context, SampleImagesActivity::class.java))
        },
        icon = Icons.Rounded.Casino,
        descriptionResourceId = R.string.image_loading_activity_icon_description_sample_images,
    )
}

@Composable
fun ButtonWithIcon(
    onClick: () -> Unit,
    icon: ImageVector,
    descriptionResourceId: Int,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(width = 200.dp, height = 60.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(200.dp)
        ) {
            Icon(
                icon,
                contentDescription = stringResource(id = descriptionResourceId),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.padding(10.dp))
            Text(text = stringResource(id = descriptionResourceId))
        }
    }
    Spacer(modifier = Modifier.padding(20.dp))
}

@Composable
fun LoadImageFromGalleryBtn() {
    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract =
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    ButtonWithIcon(
        onClick = { launcher.launch("image/*") },
        icon = Icons.Rounded.Collections,
        descriptionResourceId = R.string.image_loading_activity_icon_description_gallery,
    )
    imageUri?.let { uri ->
        val bitmap = imageUriToBitmap(context, uri)
        val recognizer = SudokuBoardRecognizer(context)
        // recognizer.setImageFromResource(R.drawable.sudokuboard1)
        recognizer.setImageFromBitmap(bitmap)
        recognizer.execute()
        val predictionOutput = recognizer.predictionOutput

        Log.d("OpenCV", predictionOutput.toString())

        if (recognizer.flagDebugActivity) {
            DebugImage(recognizer.debugImage)
        } else {
            context.startActivity(
                Intent(context, MainActivity::class.java).putIntegerArrayListExtra(
                    SUDOKU_BOARD_KEY, ArrayList(predictionOutput)
                )
            )
        }
    }
}

@Composable
fun LaunchCameraBtn(
    displaySnackbar: (message: String) -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        when (isGranted) {
            true -> context.startActivity(Intent(context, CameraActivity::class.java))
            false -> displaySnackbar("Camera permission is required for importing with photo")
        }
    }

    ButtonWithIcon(
        onClick = { launcher.launch(Manifest.permission.CAMERA) },
        icon = Icons.Rounded.PhotoCamera,
        descriptionResourceId = R.string.image_loading_activity_icon_description_camera,
    )
}

fun imageUriToBitmap(context: Context, uri: Uri): Bitmap {
    return if (Build.VERSION.SDK_INT < 28) {
        MediaStore.Images
            .Media.getBitmap(context.contentResolver, uri)
    } else {
        val source = ImageDecoder
            .createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.isMutableRequired = true
        }
    }
}
