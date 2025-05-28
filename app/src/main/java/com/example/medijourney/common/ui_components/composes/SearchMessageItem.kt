package com.example.medijourney.common.ui_components.composes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.example.medijourney.common.constants.proximaNovaFamily
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.example.medijourney.modules.chat.search_message.SearchMessageViewModel

@Composable
fun SearchMessageItem(itemModel: DynamicUIItem, onSelect: () -> Unit) {

    // Properties
    var textList by rememberSaveable { mutableStateOf<List<MTextStyle>>(emptyList()) }

    // AnnotatedString
    LaunchedEffect(itemModel.additionalData) {
        val additionalData = itemModel.additionalData as? Map<*, *>
        val list = additionalData?.get(SearchMessageViewModel.TEXT_LIST) as? List<*> ?: return@LaunchedEffect
        textList = list.map {
            it as MTextStyle
        }
    }

    // Content
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onSelect
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemModel.title?.let {
                Text(
                    text = it.text,
                    fontFamily = proximaNovaFamily,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Bold,
                    fontSize = TextUnit(16f, TextUnitType.Sp)
                )
            }

            if (textList.isNotEmpty()) {
                Text(
                    buildAnnotatedString {
                        textList.forEach {
                            withStyle(style = SpanStyle(
                                fontFamily = proximaNovaFamily,
                                fontStyle = FontStyle.Normal,
                                fontWeight = FontWeight.Normal,
                                fontSize = TextUnit(16f, TextUnitType.Sp),
                                background = colorResource(it.color))
                            ) {
                                append(it.text)
                            }
                        }
                    }
                )
            }

            itemModel.description?.let {
                Text(
                    text = it.text,
                    fontFamily = proximaNovaFamily,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Normal,
                    fontSize = TextUnit(16f, TextUnitType.Sp)
                )
            }
        }
    }
}

