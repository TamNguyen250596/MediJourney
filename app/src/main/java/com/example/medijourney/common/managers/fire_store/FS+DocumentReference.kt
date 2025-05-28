package com.example.medijourney.common.managers.fire_store

import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.managers.realm.RealmCycle
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import io.realm.kotlin.types.RealmObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

fun <T> DocumentReference.observe(
    clazz: Class<T>, primaryKey: Any? = null, observer: Class<*>? = null,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
): FireStoreListenerInfo? where T : RealmCycle, T : RealmObject {
    if (FireStoreManager.checkCachedListener(this.hashCode())) {
        return null
    }

    val listenerRegistration = addSnapshotListener { snapshot, error ->
        coroutineScope.launch {
            if (error != null) {
                println("Error: $error")
            }
            snapshot?.let {
                if (it.exists()) {
                    val data = it.data
                    if (data != null) {
                        RealmManager.create(clazz, data)
                    }
                } else {
                    primaryKey?.let {
                        RealmManager.delete(clazz, primaryKey)
                    }
                }
            }
        }
    }

    val info = FireStoreListenerInfo(this.hashCode(), listenerRegistration, observer?.simpleName)
    FireStoreManager.addListener(info)
    return info
}

fun <T> DocumentReference.get(
    clazz: Class<T>, coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) where T : RealmCycle, T : RealmObject {

    get().addOnCompleteListener {
        coroutineScope.launch {
            if (it.result.exists()) {
                it.result.data?.let { data ->
                    RealmManager.create(clazz, data)
                }
            }
        }
    }
}

fun DocumentReference.addListener(
    completion: ((DocumentSnapshot) -> Unit)
): FireStoreListenerInfo? {
    if (FireStoreManager.checkCachedListener(this.hashCode())) {
        return null
    }

    val listenerRegistration = addSnapshotListener { snapshot, error ->
        if (error != null) {
            println("Error: $error")
        }
        snapshot?.let {
            completion.invoke(it)
        }
    }

    val info = FireStoreListenerInfo(this.hashCode(), listenerRegistration)
    FireStoreManager.addListener(info)
    return info
}

suspend fun DocumentReference.awaitGet(): DocumentSnapshot = suspendCancellableCoroutine { cont ->
    get().addOnSuccessListener {
        cont.resume(it)
    }.addOnFailureListener {
        cont.resumeWithException(it)
    }
}

suspend fun DocumentReference.awaitSet(data: Map<String, Any>): MutableMap<String, Any> = suspendCancellableCoroutine { cont ->
    val id = this.id
    set(data).addOnSuccessListener {
        val mutableMap = data.toMutableMap()
        mutableMap["id"] = id
        cont.resume(mutableMap)
    }.addOnFailureListener {
        cont.resumeWithException(it)
    }
}

fun DocumentReference.remove() {
    FireStoreManager.removeListener(this)
}