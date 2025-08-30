package com.example.medijourney.modules.chat.ai_chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.medijourney.common.helpers.FragmentHelper
import com.example.medijourney.common.ui_components.composes.CIndicator
import com.example.medijourney.common.ui_components.composes.MessageInputField
import com.example.medijourney.modules.chat.message.sub_views.MessageListView

class AIChatFragment : Fragment() {

    // Properties
    private val viewModel: AIChatViewModel by viewModels()

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentHelper.createBaseComposeView(inflater, container) {
            AIChatScreen(viewModel)
        }
    }
}

@Composable
fun AIChatScreen(viewModel: AIChatViewModel) {

    // Properties
    val itemModels by viewModel.itemModels.collectAsState()
    val listState = rememberLazyListState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                lastIndex?.let {
                    viewModel.fetchNextPage(lastIndex)
                }
            }
    }

    LaunchedEffect(itemModels.size) {
        if (viewModel.shouldScrollToBottom) {
            listState.animateScrollToItem(0)
            viewModel.shouldScrollToBottom = false
        }
    }

    // Content
    ConstraintLayout(
        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)
    ) {
        val (messageList, inputFieldColumn, indicator) = createRefs()

        MessageListView(
            modifier = Modifier.constrainAs(messageList) {
                top.linkTo(parent.top)
                bottom.linkTo(inputFieldColumn.top, 4.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                height = Dimension.fillToConstraints
                width = Dimension.matchParent
            },
            listState = listState,
            highlightMessageIndex = null,
            itemModels = itemModels,
            onAction = { _, _ -> }
        )

        MessageInputField(
            modifier = Modifier.constrainAs(inputFieldColumn) {
                top.linkTo(messageList.bottom)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                height = Dimension.wrapContent
            },
            onSend = { message, imageUri ->
                viewModel.sendMessage(message, imageUri)
            }
        )

        if (isLoading) {
            CIndicator(
                modifier = Modifier.constrainAs(indicator) {
                    centerHorizontallyTo(parent)
                    centerVerticallyTo(parent)
                    width = Dimension.value(48.dp)
                    height = Dimension.value(48.dp)
                }
            )
        }
    }
}