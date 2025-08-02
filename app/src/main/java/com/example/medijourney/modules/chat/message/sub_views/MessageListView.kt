package com.example.medijourney.modules.chat.message.sub_views

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.constants.MessageMenuAction
import com.example.medijourney.common.models.item_models.DynamicUIItem
import kotlinx.coroutines.delay

@Composable
fun MessageListView(modifier: Modifier,
                    listState: LazyListState,
                    highlightMessageIndex: Int?,
                    itemModels: List<DynamicUIItem>,
                    onAction: (MessageMenuAction, DynamicUIItem) -> Unit) {

    // Content
    LazyColumn(
        modifier = modifier,
        state = listState,
        reverseLayout = true,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Bottom)
    ) {
        itemsIndexed(
            items = itemModels,
            key = { _, item -> item.itemTag }
        ) { index, it ->

            // Properties
            val isComingMessage = it.getAdditionalValue(Constants.IS_INCOMING_MESSAGE) as? Boolean == true
            val isFirstConsecutiveFromUser = it.getAdditionalValue(Constants.IS_FIRST_CONSECUTIVE_FROM_USER) as? Boolean == true
            var isHighlight by remember { mutableStateOf(false) }
            val color by animateColorAsState(
                targetValue = if (isHighlight) colorResource(R.color.sky_blue_color) else colorResource(R.color.white),
                animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
                label = "color"
            )
            val itemModifier = Modifier.fillMaxWidth().padding(top = if (index == itemModels.size - 1) 80.dp else 0.dp)
            val contentRowModifier = Modifier.fillMaxWidth().drawBehind {
                drawRect(color)
            }

            // LaunchedEffect
            LaunchedEffect(highlightMessageIndex) {
                isHighlight = index == highlightMessageIndex
                delay(1000)
                isHighlight = false
            }

            if (isComingMessage) {
                val boxModifier = if (isFirstConsecutiveFromUser) {
                    Modifier
                        .background(
                            color = colorResource(R.color.soft_beige),
                            shape = RoundedCornerShape(
                                topStart = 8.dp,
                                topEnd = 8.dp,
                                bottomStart = 0.dp,
                                bottomEnd = 8.dp
                            )
                        )
                } else {
                    Modifier
                        .background(
                            color = colorResource(R.color.soft_beige),
                            shape = RoundedCornerShape(8.dp)
                        )
                }

                IncomingMessageItem(
                    modifier = itemModifier,
                    contentRowModifier = contentRowModifier,
                    boxModifier = boxModifier,
                    itemModel = it,
                    onAction = onAction
                )

            } else {
                val boxModifier = if (isFirstConsecutiveFromUser) {
                    Modifier
                        .background(
                            color = colorResource(R.color.teal_blue),
                            shape = RoundedCornerShape(
                                topStart = 8.dp,
                                topEnd = 8.dp,
                                bottomStart = 8.dp,
                                bottomEnd = 0.dp
                            )
                        )
                } else {
                    Modifier
                        .background(
                            color = colorResource(R.color.teal_blue),
                            shape = RoundedCornerShape(8.dp)
                        )
                }

                OutgoingMessageItem(
                    itemModifier = itemModifier,
                    contentRowModifier = contentRowModifier,
                    boxModifier = boxModifier,
                    itemModel = it,
                    onAction = onAction
                )
            }

        }
    }
}