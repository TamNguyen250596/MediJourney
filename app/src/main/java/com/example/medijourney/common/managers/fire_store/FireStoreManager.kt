package com.example.medijourney.common.managers.fire_store

import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.installations.FirebaseInstallations
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object FireStoreManager {

    // Properties
    private var listeners: MutableList<FireStoreListenerInfo> = mutableListOf()
    private var newListeners = ConcurrentHashMap.newKeySet<FSListener>()

    // Build DocumentReference
    fun buildDoc(vararg nodes: Pair<FireStoreCollection, String?>): DocumentReference {
        require(nodes.isNotEmpty()) { "buildDocRef requires a non-empty list of collection-document pairs" }

        val db = Firebase.firestore
        var docRef = nodes[0].second?.let {
            db.collection(nodes[0].first.name.lowercase()).document(it)
        } ?: run {
            db.collection(nodes[0].first.name.lowercase()).document()
        }

        for (i in 1 until nodes.size) {
            val (collection, documentId) = nodes[i]
            documentId?.let {
                docRef = docRef.collection(collection.name.lowercase()).document(documentId)
            } ?: run {
                docRef = docRef.collection(collection.name.lowercase()).document()
            }
        }

        return docRef
    }

    fun buildUserDocRef(vararg nodes: Pair<FireStoreCollection, String?>): DocumentReference {
        require(nodes.isNotEmpty()) { "buildDocRef requires a non-empty list of collection-document pairs" }

        val db = Firebase.firestore
        val currentUserCode = FirebaseAuthManager.getCurrentUserCode() ?: ""
        var docRef = db.collection(FireStoreCollection.USER_MEMBERS.name.lowercase())
            .document(currentUserCode)

        nodes.forEach {
            val (collection, documentId) = it
            documentId?.let {
                docRef = docRef.collection(collection.name.lowercase()).document(documentId)
            } ?: run {
                docRef = docRef.collection(collection.name.lowercase()).document()
            }
        }
        return docRef
    }

    // Build Query
    fun buildCollection(collection: FireStoreCollection): Query {
        val db = Firebase.firestore
        val ref = db.collection(collection.name.lowercase())
        return ref
    }

    fun buildSubCollectionRef(subCollection: FireStoreCollection, vararg nodes: Pair<FireStoreCollection, String>): Query {
        require(nodes.isNotEmpty()) { "buildDocRef requires a non-empty list of collection-document pairs" }

        val db = Firebase.firestore
        var ref = db.collection(nodes[0].first.name.lowercase())
            .document(nodes[0].second)

        for (i in 1 until nodes.size) {
            val (collection, documentId) = nodes[i]
            ref = ref.collection(collection.name.lowercase()).document(documentId)
        }
        return ref.collection(subCollection.name.lowercase())
    }

    fun buildUserCollectionRef(collection: FireStoreCollection): Query {
        val db = Firebase.firestore
        val currentUserCode = FirebaseAuthManager.getCurrentUserCode() ?: ""
        val ref = db.collection(FireStoreCollection.USER_MEMBERS.name.lowercase())
            .document(currentUserCode)
            .collection(collection.name.lowercase())
        return ref
    }

    // Functions
    fun checkCachedListener(query: Int): Boolean {
        synchronized(this) {
            return listeners.count { it.queryId == query } > 0
        }
    }

    fun checkCachedListener(listener: FSListener) : Boolean {
        return newListeners.contains(listener)
    }

    fun addListener(listenerInfo: FireStoreListenerInfo) {
        synchronized(this) {
            listeners.add(listenerInfo)
        }
    }

    fun addListener(listener: FSListener) {
        newListeners.add(listener)
    }

    fun removeListener(listener: FSListener) {
        listener.registration?.remove()
        newListeners.remove(listener)
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
        for (listener in newListeners) {
            listener.registration?.remove()
        }
        newListeners.clear()
    }

    // DocumentReference - Build
    fun buildDoc(collection: FireStoreCollection, documentId: String): DocumentReference {
        val db = Firebase.firestore
        val docRef = db.collection(collection.name.lowercase()).document(documentId)
        return docRef
    }

    // DocumentReference - Update
    suspend fun createDoc(collection: FireStoreCollection, documentId: String? = null, data: Map<String, Any>): Boolean {
        val realmObject = collection.getRealmObject() ?: return false
        val db = Firebase.firestore
        val docRef = if (documentId != null) {
            db.collection(collection.name.lowercase()).document(documentId)
        } else {
            db.collection(collection.name.lowercase()).document()
        }
        val data = data.toMutableMap()
        data["id"] = documentId ?: docRef.id

        val result = suspendCancellableCoroutine { cont ->
            docRef.set(data).addOnCompleteListener {
                cont.resume(it.isSuccessful)
            }
        }
        if (result) {
            RealmManager.write(realmObject, data)
        }
        return result
    }

    // DocumentReference - Update
    suspend fun updateDoc(collection: FireStoreCollection, documentId: String, data: Map<String, Any>): Boolean {
        val realmObject = collection.getRealmObject() ?: return false
        val docRef = buildDoc(collection, documentId)

        val result = suspendCancellableCoroutine { cont ->
            docRef.update(data).addOnCompleteListener {
                cont.resume(it.isSuccessful)
            }
        }
        if (result) {
            RealmManager.write(realmObject, data)
        }
        return result
    }

    // DocumentReference - Get
    suspend fun getDoc(collection: FireStoreCollection, documentId: String): DocumentSnapshot {
        val docRef = buildDoc(collection, documentId)

        return suspendCancellableCoroutine { cont ->
            docRef.get().addOnSuccessListener {
                cont.resume(it)
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
        }
    }

    // DocumentReference - Observe
    suspend fun observeDoc(collection: FireStoreCollection, documentId: String) {
        val docRef = buildDoc(collection, documentId)
        val listenerWrapper = FSListener(FSListerType.DOCUMENT(docRef))
        if (checkCachedListener(listenerWrapper)) return
        val realmObject = collection.getRealmObject() ?: return

        val snapshotFlow = callbackFlow {
            val registration: ListenerRegistration = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snapshot)
            }

            listenerWrapper.registration = registration
            addListener(listenerWrapper)

            awaitClose {
                removeListener(listenerWrapper)
            }
        }

        snapshotFlow.collect { snapshot ->
            snapshot?.let {
                if (it.exists()) {
                    val data = it.data ?: return@let
                    RealmManager.write(realmObject, data)
                } else {
                    RealmManager.delete(realmObject, it.id)
                }
            }
        }
    }

    // DocumentReference - Delete
    suspend fun deleteDoc(collection: FireStoreCollection, documentId: String): Boolean {
        val docRef = buildDoc(collection, documentId)
        return suspendCancellableCoroutine { cont ->
            docRef.delete().addOnCompleteListener {
                cont.resume(it.isSuccessful)
            }
        }
    }

    // Collection - Build
    fun buildQuery(collection: FireStoreCollection, filterBuilder: FSFilterBuilder?): Query {
        val db = Firebase.firestore
        val ref = db.collection(collection.name.lowercase())
        var query: Query = ref
        filterBuilder?.let {
            for (filter in filterBuilder.build()) {
                query = query.where(filter)
            }
        }
        return query
    }

    // Collection - Get
    suspend fun getCollection(collection: FireStoreCollection, filterBuilder: FSFilterBuilder?): QuerySnapshot {
        val query = buildQuery(collection, filterBuilder)

        return suspendCancellableCoroutine { cont ->
            query.get().addOnSuccessListener {
                cont.resume(it)
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
        }
    }

    // Collection - Observe
    suspend fun observeCollection(collection: FireStoreCollection, filterBuilder: FSFilterBuilder? = null) {
        val query = buildQuery(collection, filterBuilder)
        val listenerWrapper = FSListener(FSListerType.COLLECTION(query))
        if (checkCachedListener(listenerWrapper)) return
        val realmObject = collection.getRealmObject() ?: return

        val snapshotFlow = callbackFlow {
            val registration: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snapshot)
            }

            listenerWrapper.registration = registration
            addListener(listenerWrapper)

            awaitClose {
                removeListener(listenerWrapper)
            }
        }

        snapshotFlow.collect { snapshot ->
            val snapshot = snapshot ?: return@collect
            val dataList: MutableList<Map<String, Any>> = mutableListOf()
            val removeIds = mutableListOf<String>()

            snapshot.documentChanges.forEach {
                when (it.type) {
                    DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                        if (it.document.exists()) {
                            dataList.add(it.document.data)
                        }
                    }

                    DocumentChange.Type.REMOVED -> {
                        removeIds.add(it.document.id)
                    }
                }
            }

            if (dataList.isNotEmpty()) {
                RealmManager.write(realmObject, dataList)
            }
            if (removeIds.isNotEmpty()) {
                RealmManager.delete(realmObject, removeIds)
            }
        }
    }

    suspend fun observeRedundantData() {
        val userCode = FirebaseAuthManager.getCurrentUserCode() ?: return

        val id = suspendCancellableCoroutine { count ->
            FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    count.resume(task.result)
                } else {
                    count.resume(null)
                }
            }
        }
        val fid = id ?: return
        val query = buildQuery(
            FireStoreCollection.USER_REDUNDANT_DATA,
            filterBuilder = FSFilterBuilder()
                .equalTo(Constants.FID, fid)
                .equalTo("user_id", userCode)
        )
        val listenerWrapper = FSListener(FSListerType.COLLECTION(query))
        if (checkCachedListener(listenerWrapper)) return

        val snapshotFlow = callbackFlow {
            val registration: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snapshot)
            }

            listenerWrapper.registration = registration
            addListener(listenerWrapper)

            awaitClose {
                removeListener(listenerWrapper)
            }
        }

        snapshotFlow.collect { snapshot ->
            val snapshot = snapshot ?: return@collect
            for (doc in snapshot.documents) {
                val data = doc.data ?: return@collect
                val dataType = data["data_type"] as? String ?: return@collect
                val dataIds = data["data_ids"] as? List<*> ?: return@collect
                val realmObject = FireStoreCollection.valueOf(dataType).getRealmObject() ?: return@collect

                RealmManager.delete(realmObject, dataIds)
                buildDoc(FireStoreCollection.USER_REDUNDANT_DATA, doc.id).delete()
            }
        }
    }
}