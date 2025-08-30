package com.example.medijourney.common.respository

import kotlinx.coroutines.CoroutineScope

interface FirestoreListener {
    fun observe(coroutine: CoroutineScope)
}