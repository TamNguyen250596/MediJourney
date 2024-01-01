package com.example.medijourney.common.ui_components.composes

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import com.example.medijourney.R
import com.example.medijourney.common.constants.proximaNovaFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(initialSelectedDateMillis: Long? = null,
                    onSelectableDate: (Long) -> Boolean,
                    onSelectableYear: (Int) -> Boolean,
                    onDateSelected: (Long?) -> Unit,
                    onDismiss: () -> Unit) {

    // Properties
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDateMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return onSelectableDate(utcTimeMillis)
            }

            override fun isSelectableYear(year: Int): Boolean {
                return onSelectableYear(year)
            }
        }
    )

    // Content
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text(
                    text = "OK",
                    fontFamily = proximaNovaFamily,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Bold,
                    fontSize = TextUnit(16f, TextUnitType.Sp)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.close),
                    fontFamily = proximaNovaFamily,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Bold,
                    fontSize = TextUnit(16f, TextUnitType.Sp)
                )
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = colorResource(R.color.white)
            ),
            showModeToggle = false
        )
    }
}