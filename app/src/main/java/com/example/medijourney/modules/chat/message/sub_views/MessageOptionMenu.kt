package com.example.medijourney.modules.chat.message.sub_views

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.constants.MessageMenuAction
import com.example.medijourney.common.models.item_models.DynamicUIItem

@Suppress("UNCHECKED_CAST")
@Composable
fun MessageOptionMenu(expanded: Boolean,
                      itemModel: DynamicUIItem,
                      onDismissRequest: () -> Unit,
                      onAction: (MessageMenuAction) -> Unit) {

    // Properties
    val actions by remember {
        mutableStateOf(itemModel.getAdditionalValue(Constants.MESSAGE_MENU_ACTIONS) as? List<MessageMenuAction> ?: emptyList())
    }

    // Content
    if (actions.isNotEmpty()) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest
        ) {

            actions.forEachIndexed { index, messageMenuAction ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(messageMenuAction.value),
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.Normal,
                            fontSize = TextUnit(16f, TextUnitType.Sp),
                            color = colorResource(R.color.black),
                            textAlign = TextAlign.Center
                        )
                    },
                    onClick = {
                        onAction(messageMenuAction)
                    }
                )

                if (index != actions.size - 1) {
                    HorizontalDivider()
                }
            }
        }
    }
}