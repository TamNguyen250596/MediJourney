package com.example.medijourney.common.managers.fire_store

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

data class FireStoreListenerInfo(
    var queryId: Int,
    var listener: ListenerRegistration,
    var firstObserverId: String? = null
)

sealed class FSListerType {
    data class DOCUMENT(val docRef: DocumentReference) : FSListerType()
    data class COLLECTION(val query: Query) : FSListerType()
}

data class FSListener(
    val listenerType: FSListerType,
    var registration: ListenerRegistration? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FSListener) return false

        return when {
            listenerType is FSListerType.DOCUMENT && other.listenerType is FSListerType.DOCUMENT ->
                listenerType.docRef == other.listenerType.docRef
            listenerType is FSListerType.COLLECTION && other.listenerType is FSListerType.COLLECTION ->
                listenerType.query == other.listenerType.query
            else -> false
        }
    }

    override fun hashCode(): Int {
        return when (listenerType) {
            is FSListerType.DOCUMENT -> listenerType.docRef.hashCode()
            is FSListerType.COLLECTION -> listenerType.query.hashCode()
        }
    }
}