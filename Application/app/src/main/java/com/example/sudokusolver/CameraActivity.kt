package com.example.sudokusolver

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.AspectRatio.RATIO_16_9
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.sudokusolver.ui.theme.SudokuSolverTheme
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CameraActivity : ComponentActivity() {
    private lateinit var outputDirectory: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        outputDirectory = getOutputDirectory()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        setContent {
            val scaffoldState = rememberScaffoldState()
            val snackbarCoroutineScope = rememberCoroutineScope()
            val (photoUri, setPhotoUri) = remember {
                mutableStateOf<Uri?>(null)
            }

            SudokuSolverTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Scaffold(
                        scaffoldState = scaffoldState,
                    ) {
                        when (photoUri) {
                            null -> CameraPreview(
                                cameraProviderFuture = cameraProviderFuture,
                                takePhoto = { imageCapture ->
                                    takePhoto(
                                        imageCapture = imageCapture,
                                        setPhotoUri = setPhotoUri,
                                        displaySnackbar = { msg ->
                                            snackbarCoroutineScope.launch {
                                                scaffoldState.snackbarHostState.showSnackbar(
                                                    message = msg,
                                                )
                                            }
                                        }
                                    )
                                },
                            )
                            else -> DisplayPhoto(
                                photoUri = photoUri,
                                retakePhoto = {
                                    // When retake function is executed we don't add the activity on the backstack
                                    // by setting the flag FLAG_ACTIVITY_NO_HISTORY.
                                    // This is done to provide better user experience when the user
                                    // is clicking the back button on their phone.
                                    startActivity(
                                        Intent(
                                            this,
                                            CameraActivity::class.java
                                        ).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun takePhoto(
        imageCapture: ImageCapture,
        setPhotoUri: (Uri) -> Unit,
        displaySnackbar: (message: String) -> Unit
    ) {
        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(e: ImageCaptureException) {
                    displaySnackbar("Photo capture failed: ${e.message}")
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    Log.d(TAG, "Photo capture succeeded: $savedUri")
                    setPhotoUri(savedUri)
                }
            }
        )
    }

    // Return the File where the captured photo can be saved
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}

// Binds the Camera Provider to the live preview
fun bindPreview(
    cameraProvider: ProcessCameraProvider,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    imageCapture: ImageCapture
) {
    val preview: Preview = Preview.Builder()
        .setTargetAspectRatio(RATIO_16_9)
        .build()

    val cameraSelector: CameraSelector = CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
        .build()

    preview.setSurfaceProvider(previewView.surfaceProvider)

    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
}

@Composable
fun CameraPreview(
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    takePhoto: (ImageCapture) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = ImageCapture.Builder()
        .build()

    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                scaleType = PreviewView.ScaleType.FILL_START
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                post {
                    cameraProviderFuture.addListener(
                        Runnable {
                            val cameraProvider = cameraProviderFuture.get()
                            bindPreview(
                                cameraProvider,
                                lifecycleOwner,
                                this,
                                imageCapture
                            )
                        },
                        ContextCompat.getMainExecutor(context)
                    )
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 30.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { takePhoto(imageCapture) },
        ) {
            Text(text = "Capture Photo")
        }
    }
}

// Displays the captured photo and user can decide to use photo or retake
@Composable
private fun DisplayPhoto(photoUri: Uri, retakePhoto: () -> Unit) {
    val bitmap = remember {
        mutableStateOf<Bitmap?>(null)
    }
    val context = LocalContext.current

    bitmap.value = imageUriToBitmap(context, photoUri)

    bitmap.value?.let { btm ->
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                bitmap = btm.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.weight(4f)
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(0.5f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround

            ) {
                Button(
                    onClick = { retakePhoto() },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        Icons.Rounded.Replay,
                        stringResource(id = R.string.display_photo_icon_description_retake_photo)
                    )
                }
                Spacer(modifier = Modifier.padding(15.dp))
                Button(
                    onClick = {
                        val recognizer = SudokuBoardRecognizer(context)
                        recognizer.setImageFromBitmap(btm)
                        val (predictionOutput, error) = recognizer.execute()

                        Log.d("OpenCV", predictionOutput.toString())

                        context.startActivity(
                            Intent(context, MainActivity::class.java).putExtra(
                                SUDOKU_BOARD_KEY, ArrayList(predictionOutput)
                            )
                        )
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        Icons.Rounded.Done,
                        stringResource(id = R.string.display_photo_icon_description_accept)
                    )
                }
            }
        }
    }
}
