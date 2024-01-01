package com.example.medijourney.common.ui_components.composes

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.medijourney.R
import com.example.medijourney.common.helpers.ImageHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomMediaPicker(onDismissRequest: () -> Unit,
                      onMediaSelected: (Uri?) -> Unit,
                      onImageCaptured: (Uri?) -> Unit) {

    // Properties
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        onMediaSelected(uri)
    }
    var showCamera by remember { mutableStateOf(false) }

    // Content
    if (showCamera) {
        CameraPreviewWithCapture(onImageCaptured)
    } else {
        ModalBottomSheet(
            modifier = Modifier.fillMaxWidth(),
            onDismissRequest = { onDismissRequest() },
            sheetState = sheetState
        ) {
            if (hasPermission) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        modifier = Modifier
                            .height(72.dp)
                            .weight(1f),
                        border = BorderStroke(1.dp, colorResource(id = R.color.deep_turquoise_blue_color)),
                        onClick = {
                            showCamera = true
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(id = R.drawable.ic_camera),
                                contentDescription = "",
                                colorFilter = ColorFilter.tint(colorResource(id = R.color.deep_turquoise_blue_color))
                            )
                            Text(
                                text = stringResource(R.string.camera),
                                fontStyle = FontStyle.Normal,
                                fontWeight = FontWeight.Bold,
                                fontSize = TextUnit(16f, TextUnitType.Sp),
                                color = colorResource(R.color.deep_turquoise_blue_color),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .height(72.dp)
                            .weight(1f),
                        border = BorderStroke(1.dp, colorResource(id = R.color.deep_turquoise_blue_color)),
                        onClick = {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(id = R.drawable.ic_gallery),
                                contentDescription = "",
                                colorFilter = ColorFilter.tint(colorResource(id = R.color.deep_turquoise_blue_color))
                            )
                            Text(
                                text = stringResource(R.string.gallery),
                                fontStyle = FontStyle.Normal,
                                fontWeight = FontWeight.Bold,
                                fontSize = TextUnit(16f, TextUnitType.Sp),
                                color = colorResource(R.color.deep_turquoise_blue_color),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.please_grant_camera_permission_title),
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Bold,
                        fontSize = TextUnit(16f, TextUnitType.Sp),
                        color = colorResource(R.color.deep_turquoise_blue_color),
                        textAlign = TextAlign.Center
                    )
                    OutlinedButton(
                        border = BorderStroke(1.dp, colorResource(id = R.color.deep_turquoise_blue_color)),
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(R.string.grant_camera_permission),
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.Bold,
                            fontSize = TextUnit(16f, TextUnitType.Sp),
                            color = colorResource(R.color.deep_turquoise_blue_color),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreviewWithCapture(onImageCaptured: (Uri?) -> Unit) {

    // Properties
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }
    val previewView = remember { PreviewView(context) }

    // Content
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            factory = { previewView.also {
                it.controller = cameraController
                cameraController.bindToLifecycle(lifecycleOwner)
            } },
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
            onClick = {
                val mainExecutor = ContextCompat.getMainExecutor(context)
                cameraController.takePicture(mainExecutor, object :
                    ImageCapture.OnImageCapturedCallback() {

                    override fun onCaptureSuccess(image: ImageProxy) {
                        super.onCaptureSuccess(image)
                        val uri = ImageHelper.saveImageProxyToGallery(context, image)
                        onImageCaptured(uri)
                    }
                })
            }
        ) {
            Image(
                modifier = Modifier.size(48.dp),
                painter = painterResource(id = R.drawable.ic_camera),
                contentDescription = "",
                colorFilter = ColorFilter.tint(colorResource(id = R.color.white))
            )
        }
    }
}