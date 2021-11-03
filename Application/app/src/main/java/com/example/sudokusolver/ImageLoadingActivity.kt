package com.example.sudokusolver

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.sudokusolver.ui.theme.SudokuSolverTheme

class ImageLoadingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
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
                        ButtonWithIcon(
                            onClick = {/*TODO: Launch Camera*/ },
                            icon = Icons.Rounded.PhotoCamera,
                            descriptionResourceId = R.string.image_loading_activity_icon_description_camera,
                        )
                        LoadImageFromGalleryBtn()
                        ButtonWithIcon(
                            onClick = { /*TODO: Load hardcoded/random board*/ },
                            icon = Icons.Rounded.Casino,
                            descriptionResourceId = R.string.image_loading_activity_icon_description_random,
                        )
                    }
                }
            }
        }
    }
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
    val bitmap = remember {
        mutableStateOf<Bitmap?>(null)
    }
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
        bitmap.value = imageUriToBitmap(context, uri)
        bitmap.value?.let { btm ->
            // TODO: Remove this image. This image should be processed to an array of ints.
            Image(
                bitmap = btm.asImageBitmap(),
                contentDescription = "image loaded",
                modifier = Modifier.size(400.dp)
            )
        }
    }

}

fun imageUriToBitmap(context: Context, uri: Uri): Bitmap {
    return if (Build.VERSION.SDK_INT < 28) {
        MediaStore.Images
            .Media.getBitmap(context.contentResolver, uri)

    } else {
        val source = ImageDecoder
            .createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
}