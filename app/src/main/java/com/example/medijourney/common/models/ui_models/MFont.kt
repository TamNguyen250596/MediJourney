package com.example.medijourney.common.models.ui_models

import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

data class MFont(val size: Float, val style: FontStyle, val weight: FontWeight) {

    companion object {
        fun regular(size: Float): MFont = MFont(size, FontStyle.Normal, FontWeight.Normal)
        fun bold(size: Float): MFont = MFont(size, FontStyle.Normal, FontWeight.Bold)
        fun italic(size: Float): MFont = MFont(size, FontStyle.Italic, FontWeight.Normal)
        fun boldItalic(size: Float): MFont = MFont(size, FontStyle.Italic, FontWeight.Bold)
        fun light(size: Float): MFont = MFont(size, FontStyle.Normal, FontWeight.Light)
        fun medium(size: Float): MFont = MFont(size, FontStyle.Normal, FontWeight.Medium)
        fun semiBold(size: Float): MFont = MFont(size, FontStyle.Normal, FontWeight.SemiBold)
        fun custom(size: Float, style: FontStyle, weight: FontWeight): MFont = MFont(size, style, weight)
    }
}