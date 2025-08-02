package com.example.medijourney.modules.chat.message.sub_views

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.constants.MessageMenuAction
import com.example.medijourney.common.managers.firebase_storage.FirebaseStorageManager
import com.example.medijourney.common.models.item_models.DynamicUIItem
import kotlinx.coroutines.delay
import java.text.BreakIterator
import java.text.StringCharacterIterator

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun IncomingMessageItem(modifier: Modifier,
                        contentRowModifier: Modifier,
                        boxModifier: Modifier,
                        itemModel: DynamicUIItem,
                        onAction: (MessageMenuAction, DynamicUIItem) -> Unit) {

    // Properties
    var secondaryImageBitmap by rememberSaveable { mutableStateOf<Uri?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val date by remember { mutableStateOf(itemModel.getAdditionalValue(Constants.MESSAGE_DATE) as? String) }
    var enableTypeAnimation by rememberSaveable {
        mutableStateOf(itemModel.getAdditionalValue(Constants.ENABLE_TYPED_ANIMATION) as? Boolean == true)
    }
    val breakIterator = remember(itemModel.description?.text) { BreakIterator.getCharacterInstance() }
    val typingDelayInMs = 50L
    var substringText by remember {
        mutableStateOf("")
    }

    // LaunchedEffect
    LaunchedEffect(itemModel.secondaryImage?.url) {
        val secondaryImageUri = itemModel.secondaryImage?.url ?: return@LaunchedEffect

        FirebaseStorageManager.downloadImage(secondaryImageUri) { downloadedBitmap ->
            secondaryImageBitmap = downloadedBitmap ?: run {
                secondaryImageUri.toUri()
            }
        }
    }

    LaunchedEffect(itemModel.description?.text) {
        if (!enableTypeAnimation) return@LaunchedEffect
        val text = itemModel.description?.text ?: return@LaunchedEffect
        delay(1000)
        breakIterator.text = StringCharacterIterator(text)

        var nextIndex = breakIterator.next()
        while (nextIndex != BreakIterator.DONE) {
            substringText = text.subSequence(0, nextIndex).toString()
            nextIndex = breakIterator.next()
            delay(typingDelayInMs)
        }
        enableTypeAnimation = false
    }

    // Content
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemModel.secondaryDescription?.let {
            MessageDateHeader(it.text)
        }

        Row(
            modifier = contentRowModifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {

            SenderAvatar(itemModel)

            Card(
                modifier = boxModifier,
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent,
                ),
                onClick = {
                    expanded = !expanded
                }
            ) {
                Column(
                    modifier = Modifier
                        .padding(4.dp)
                        .widthIn(
                            min = 0.dp,
                            max = 280.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    itemModel.title?.let {
                        Text(
                            text = it.text,
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.Bold,
                            fontSize = TextUnit(16f, TextUnitType.Sp),
                            color = colorResource(R.color.deep_turquoise_blue_color),
                            textAlign = TextAlign.Start
                        )
                    }

                    secondaryImageBitmap?.let {
                        GlideImage(
                            model = it,
                            contentDescription = itemModel.image?.name,
                            modifier = Modifier
                                .width(200.dp)
                                .heightIn(
                                    min = 0.dp,
                                    max = 200.dp
                                ),
                            contentScale = ContentScale.Crop
                        )
                    }

                    itemModel.description?.let {
                        if (enableTypeAnimation) {
                            Text(
                                text = substringText,
                                fontStyle = FontStyle.Normal,
                                fontWeight = FontWeight.Normal,
                                fontSize = TextUnit(16f, TextUnitType.Sp),
                                color = colorResource(R.color.black),
                                textAlign = TextAlign.Start
                            )
                        } else {
                            Text(
                                text = it.text,
                                fontStyle = FontStyle.Normal,
                                fontWeight = FontWeight.Normal,
                                fontSize = TextUnit(16f, TextUnitType.Sp),
                                color = colorResource(R.color.black),
                                textAlign = TextAlign.Start
                            )
                        }
                    }

                    date?.let {
                        Text(
                            text = it,
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.Normal,
                            fontSize = TextUnit(10f, TextUnitType.Sp),
                            color = colorResource(R.color.black),
                            textAlign = TextAlign.End
                        )
                    }
                }

                MessageOptionMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    itemModel = itemModel,
                    onAction = {
                        expanded = false
                        onAction(it, itemModel)
                    }
                )
            }
        }
    }
}