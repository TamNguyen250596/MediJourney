package com.example.medijourney.common.helpers

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import com.example.medijourney.R

object ImageHelper {

    fun isValidResource(name: String?): Boolean {
        return name?.let { getResourceIdByName(it) } != null
    }

    fun getResourceIdByName(resourceName: String): Int? {
        return try {
            R.drawable::class.java.getField(resourceName).getInt(null)
        } catch (e: Exception) {
            null
        }
    }

    fun getDrawableByName(imageName: String): Drawable? {
        val context = MediJourney.getAppContext()
        val resourceId = R.drawable::class.java.getField(imageName).getInt(null)

        return if (resourceId != 0) {
            ContextCompat.getDrawable(context, resourceId)
        } else {
            null
        }
    }

    fun saveImageProxyToGallery(context: Context, imageProxy: ImageProxy): Uri? {
        val bitmap = rotateBitmap(imageProxy.toBitmap(), 90)
        val filename = "IMG_${System.currentTimeMillis()}.jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            resolver.openOutputStream(it).use { outputStream ->
                outputStream?.let { it1 -> bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it1) }
            }
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }

        imageProxy.close()
        return uri
    }

    fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(rotationDegrees.toFloat())
        return Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )
    }
}