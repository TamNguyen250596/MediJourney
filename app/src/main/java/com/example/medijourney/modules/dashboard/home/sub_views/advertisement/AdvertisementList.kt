package com.example.medijourney.modules.dashboard.home.sub_views.advertisement

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.medijourney.common.models.item_models.ImageItemModel
import com.example.medijourney.common.ui_components.composes.VGlideImageItem
import kotlinx.coroutines.delay
import java.util.UUID
import com.example.medijourney.R

@Composable
fun AdvertisementList(viewModel: AdvertisementViewModel, onClickItem: (ImageItemModel) -> Unit) {

    // Properties
    val items by viewModel.items.collectAsState()
    val pagerState = rememberPagerState(
        initialPage = viewModel.getInitialPage()
    ) { Int.MAX_VALUE }
    val pagerIsDragged by pagerState.interactionSource.collectIsDraggedAsState()
    val pageInteractionSource = remember { MutableInteractionSource() }
    val pageIsPressed by pageInteractionSource.collectIsPressedAsState()
    val autoAdvance = !pagerIsDragged && !pageIsPressed

    // LaunchEffect
    if (autoAdvance) {
        LaunchedEffect(pagerState, pageInteractionSource) {
            while (true) {
                delay(5000)
                val nextPage = pagerState.currentPage + 1
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    // Content
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        HorizontalPager(
            modifier = Modifier
                .fillMaxSize()
                .height(240.dp),
            key = { page ->
                viewModel.getItemModel(page)?.itemTag ?: UUID.randomUUID().toString()
            },
            state = pagerState,
        ) { page ->
            val item = viewModel.getItemModel(page) ?: return@HorizontalPager
            VGlideImageItem(
                itemModel = item,
                cardShape = RoundedCornerShape(0.dp),
                imageContentMode = ContentScale.FillBounds,
                onClick = {
                    onClickItem(item)
                }
            )
        }

        Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(items.size) { iteration ->
                val color =
                    if (viewModel.getItemIndex(pagerState.currentPage) == iteration)
                        colorResource(R.color.deep_turquoise_blue_color)
                    else colorResource(R.color.gray_400)
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}