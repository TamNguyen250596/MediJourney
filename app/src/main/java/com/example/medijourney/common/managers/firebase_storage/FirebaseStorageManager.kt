package com.example.medijourney.common.managers.firebase_storage

import android.net.Uri
import androidx.core.net.toUri
import com.example.medijourney.common.managers.firebase_auth.FAManger
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import java.io.File

object FirebaseStorageManager {

    // Properties
    private val storage = Firebase.storage

    // Image
    fun saveImage(uri: Uri, imageName: String, completion: (url: String?) -> Unit) {
        val currentUserCode = FAManger.currentUserCode

        val imageRef = storage.reference
            .child(FirebaseStorageFolder.IMAGES.name.lowercase())
            .child(currentUserCode)
            .child(imageName)
        val uploadTask = imageRef.putFile(uri)
        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                completion.invoke(imageRef.path)
            } else {
                completion.invoke(null)
            }
        }
    }

    fun downloadImage(path: String, completion: (uri: Uri?) -> Unit) {
        val imageRef = storage.reference.child(path)
        imageRef.downloadUrl.addOnSuccessListener { uri ->
            completion.invoke(uri)
        }.addOnFailureListener {
            completion.invoke(null)
        }
    }

    fun downloadUserImage(imageName: String, extension: String, completion: (uri: Uri?) -> Unit) {
        val currentUserCode = FAManger.currentUserCode

        val imageRef = storage.reference
            .child(FirebaseStorageFolder.IMAGES.name.lowercase())
            .child(currentUserCode)
        val spaceRef = imageRef.child(imageName)
        val localFile = File.createTempFile("${currentUserCode}_${imageName}", extension)
        spaceRef.getFile(localFile).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                completion.invoke(localFile.toUri())
            } else {
                completion.invoke(null)
            }
        }
    }

    // HTML
    fun downloadHtml(fileName: String, completion: (uri: Uri?) -> Unit) {
        val ref = storage.reference
            .child(FirebaseStorageFolder.HTML.name.lowercase())
            .child(fileName)
        ref.downloadUrl.addOnSuccessListener { uri ->
            completion.invoke(uri)
        }.addOnFailureListener {
            completion.invoke(null)
        }
    }

    // Video
    fun downloadVideo(videoName: String, completion: (url: Uri?) -> Unit) {
        val ref = storage.reference
            .child(FirebaseStorageFolder.VIDEOS.name.lowercase())
            .child(videoName)
        ref.downloadUrl.addOnSuccessListener { uri ->
            completion.invoke(uri)
        }.addOnFailureListener {
            completion.invoke(null)
        }
    }
}