package com.example.medijourney.common.interfaces

import kotlinx.coroutines.CoroutineScope

interface FirestoreListenerInterface {
    fun observe(coroutine: CoroutineScope)
}