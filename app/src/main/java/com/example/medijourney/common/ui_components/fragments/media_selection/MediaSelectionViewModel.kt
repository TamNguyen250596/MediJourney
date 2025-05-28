package com.example.medijourney.common.ui_components.fragments.media_selection

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.activity.result.ActivityResult
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File
import java.util.UUID

class MediaSelectionViewModel: ViewModel() {

    // Properties
    val uri = MutableLiveData<Uri?>(null)

    // Functions
    fun handleResultDrawable(activityResult: ActivityResult, context: Context) {
        val drawable = if (activityResult.resultCode == Activity.RESULT_OK) {
            val data = activityResult.data
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.extras?.getParcelable("data", Bitmap::class.java)
            } else {
                @Suppress("DEPRECATION")
                data?.extras?.get("data") as? Bitmap
            }
        } else {
            null
        }

        val tempFile = File.createTempFile(UUID.randomUUID().toString(), ".png", context.cacheDir)
        tempFile.outputStream().use {
            drawable?.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        this.uri.postValue(tempFile.toUri())
    }

    fun handleResultUri(uri: Uri?) {
        this.uri.postValue(uri)
    }
}