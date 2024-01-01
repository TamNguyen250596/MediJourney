package com.example.medijourney.common.ui_components.composes

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.medijourney.R

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MessageInputField(modifier: Modifier, onSend: (String, Uri?) -> Unit) {

    // Properties
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var text by remember { mutableStateOf("") }
    var showMediaPicker by remember { mutableStateOf(false) }

    // Content
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        imageUri?.let {
            Card(
                colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.light_blue_color)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GlideImage(
                        model = it,
                        contentDescription = "",
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.3f)
                    )

                    IconButton(
                        modifier = Modifier
                            .align(Alignment.TopEnd),
                        onClick = {
                            imageUri = null
                        }
                    ) {
                        Image(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Center),
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = stringResource(R.string.delete),
                            colorFilter = ColorFilter.tint(colorResource(id = R.color.deep_turquoise_blue_color))
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                modifier = Modifier.weight(1f),
                onClick = {
                    showMediaPicker = true
                }
            ) {
                Image(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(id = R.drawable.ic_camera),
                    contentDescription = stringResource(R.string.camera),
                    colorFilter = ColorFilter.tint(colorResource(id = R.color.deep_turquoise_blue_color))
                )
            }

            OutlinedTextField(
                modifier = Modifier.widthIn(
                    min = 0.dp,
                    max = 320.dp
                ),
                value = text,
                onValueChange = {
                    text = it
                },
                maxLines = 8,
                textStyle = TextStyle(
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Normal,
                    fontSize = TextUnit(16f, TextUnitType.Sp)
                )
            )

            IconButton(
                modifier = Modifier.weight(1f),
                onClick = {
                    onSend(text, imageUri)
                    text = ""
                    imageUri = null
                }
            ) {
                Image(
                    modifier = Modifier.size(48.dp),
                    painter = painterResource(id = R.drawable.ic_send),
                    contentDescription = stringResource(R.string.send),
                    colorFilter = ColorFilter.tint(colorResource(id = R.color.deep_turquoise_blue_color))
                )
            }
        }
    }

    if (showMediaPicker) {
        BottomMediaPicker(
            onDismissRequest = { showMediaPicker = false },
            onMediaSelected = {
                imageUri = it
                showMediaPicker = false
            },
            onImageCaptured = {
                imageUri = it
                showMediaPicker = false
            }
        )
    }
}