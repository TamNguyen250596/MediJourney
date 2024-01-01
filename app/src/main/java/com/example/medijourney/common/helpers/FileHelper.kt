package com.example.medijourney.common.helpers

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

object FileHelper {
    fun getValueFromAssets(context: Context, fileName: String): String {
        val inputStream = context.assets.open(fileName)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        bufferedReader.forEachLine { stringBuilder.append(it) }
        return stringBuilder.toString()
    }
}