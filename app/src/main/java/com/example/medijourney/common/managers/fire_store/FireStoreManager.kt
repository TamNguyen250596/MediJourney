package com.example.medijourney.common.managers.fire_store

import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object FireStoreManager {

    // Properties
    private var listeners: MutableList<FireStoreListenerInfo> = mutableListOf()

    // Build query
    fun buildDocRef(vararg collectionDocumentPairs: Pair<FireStoreCollection, String?>): DocumentReference {
        require(collectionDocumentPairs.isNotEmpty()) { "buildDocRef requires a non-empty list of collection-document pairs" }

        val db = Firebase.firestore
        var docRef = collectionDocumentPairs[0].second?.let {
            db.collection(collectionDocumentPairs[0].first.collectionName).document(it)
        } ?: run {
            db.collection(collectionDocumentPairs[0].first.collectionName).document()
        }

        for (i in 1 until collectionDocumentPairs.size) {
            val (collection, documentId) = collectionDocumentPairs[i]
            documentId?.let {
                docRef = docRef.collection(collection.collectionName).document(documentId)
            } ?: run {
                docRef = docRef.collection(collection.collectionName).document()
            }
        }

        return docRef
    }

    fun buildUserDocRef(vararg collectionDocumentPairs: Pair<FireStoreCollection, String?>): DocumentReference {
        require(collectionDocumentPairs.isNotEmpty()) { "buildDocRef requires a non-empty list of collection-document pairs" }

        val db = Firebase.firestore
        val currentUserCode = FirebaseAuthManager.getCurrentUserCode() ?: ""
        var docRef = db.collection(FireStoreCollection.USER_MEMBER.collectionName)
            .document(currentUserCode)

        collectionDocumentPairs.forEach {
            val (collection, documentId) = it
            documentId?.let {
                docRef = docRef.collection(collection.collectionName).document(documentId)
            } ?: run {
                docRef = docRef.collection(collection.collectionName).document()
            }
        }
        return docRef
    }

    fun buildCollectionRef(collection: FireStoreCollection): Query {
        val db = Firebase.firestore
        val ref = db.collection(collection.collectionName)
        return ref
    }

    fun buildSubCollectionRef(subCollection: FireStoreCollection, vararg collectionDocumentPairs: Pair<FireStoreCollection, String>): Query {
        require(collectionDocumentPairs.isNotEmpty()) { "buildDocRef requires a non-empty list of collection-document pairs" }

        val db = Firebase.firestore
        var ref = db.collection(collectionDocumentPairs[0].first.collectionName)
            .document(collectionDocumentPairs[0].second)

        for (i in 1 until collectionDocumentPairs.size) {
            val (collection, documentId) = collectionDocumentPairs[i]
            ref = ref.collection(collection.collectionName).document(documentId)
        }
        return ref.collection(subCollection.collectionName)
    }

    fun buildUserCollectionRef(collection: FireStoreCollection): Query {
        val db = Firebase.firestore
        val currentUserCode = FirebaseAuthManager.getCurrentUserCode() ?: ""
        val ref = db.collection(FireStoreCollection.USER_MEMBER.collectionName)
            .document(currentUserCode)
            .collection(collection.collectionName)
        return ref
    }

    fun buildCollectionGroupRef(collection: FireStoreCollection): Query {
        val db = Firebase.firestore
        val ref = db.collectionGroup(collection.collectionName)
        return ref
    }

    // Functions
    fun checkCachedListener(query: Int): Boolean {
        synchronized(this) {
            return listeners.count { it.queryId == query } > 0
        }
    }

    fun addListener(listenerInfo: FireStoreListenerInfo) {
        synchronized(this) {
            listeners.add(listenerInfo)
        }
    }

    fun removeListeners(observer: Class<*>) {
        synchronized(this) {
            listeners = listeners.filter { listener ->
                if (listener.firstObserverId == observer.simpleName) {
                    listener.listener.remove()
                    false
                } else {
                    true
                }
            }.toMutableList()
        }
    }

    fun removeListener(queryId: Int) {
        synchronized(this) {
            listeners = listeners.filter { listener ->
                if (listener.queryId == queryId) {
                    listener.listener.remove()
                    false
                } else {
                    true
                }
            }.toMutableList()
        }
    }

    fun removeListener(query: Query) {
        synchronized(this) {
            listeners = listeners.filter { listener ->
                if (listener.queryId == query.hashCode()) {
                    listener.listener.remove()
                    false
                } else {
                    true
                }
            }.toMutableList()
        }
    }

    fun removeListener(query: DocumentReference) {
        synchronized(this) {
            listeners = listeners.filter { listener ->
                if (listener.hashCode() == query.hashCode()) {
                    listener.listener.remove()
                    false
                } else {
                    true
                }
            }.toMutableList()
        }
    }

    fun removeAllListeners() {
        synchronized(this) {
            for (listener in listeners) {
                listener.listener.remove()
            }
            listeners.clear()
        }
    }
}