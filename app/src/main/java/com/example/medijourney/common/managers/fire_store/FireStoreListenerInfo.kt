package com.example.medijourney.common.managers.fire_store

import com.google.firebase.firestore.ListenerRegistration

data class FireStoreListenerInfo(
    var queryId: Int,
    var listener: ListenerRegistration,
    var firstObserverId: String? = null
)