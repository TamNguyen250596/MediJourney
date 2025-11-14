package com.example.medijourney.common.managers.fire_store

import com.example.medijourney.common.managers.realm.RealmCycle
import com.example.medijourney.common.managers.realm.RealmManager
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.realm.kotlin.types.RealmObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun <T> Query.observe(
    clazz: Class<T>,
    observer: Class<*>? = null,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    completion: ((QuerySnapshot?) -> Unit)? = null
) where T : RealmCycle, T : RealmObject {
    if (FireStoreManager.checkCachedListener(this.hashCode())) {
        return
    }

    val listenerRegistration = addSnapshotListener { snapshot, error ->
        coroutineScope.launch {
            if (error != null) {
                println("Error: $error")
            }
            snapshot?.let {
                val addedDoc: MutableList<Map<String, Any>> = mutableListOf()
                val modifiedDoc: MutableList<Map<String, Any>> = mutableListOf()
                val realmEntryInstance = clazz.getConstructor().newInstance()
                val primaryKeyName = realmEntryInstance.primaryKey()
                val removedPrimaryKeys: MutableList<Any> = mutableListOf()

                snapshot.documentChanges.forEach {
                    when (it.type) {
                        DocumentChange.Type.ADDED -> {
                            if (it.document.exists()) {
                                addedDoc.add(it.document.data)
                            }
                        }

                        DocumentChange.Type.MODIFIED -> {
                            if (it.document.exists()) {
                                modifiedDoc.add(it.document.data)
                            }
                        }

                        DocumentChange.Type.REMOVED -> {
                            val key = it.document.data[primaryKeyName]
                            if (key != null) {
                                removedPrimaryKeys.add(key)
                            }
                        }
                    }
                }

                if (addedDoc.isNotEmpty()) {
                    RealmManager.create(clazz, addedDoc)
                }
                if (modifiedDoc.isNotEmpty()) {
                    RealmManager.update(clazz, modifiedDoc)
                }
                if (removedPrimaryKeys.isNotEmpty()) {
                    RealmManager.delete(clazz, removedPrimaryKeys)
                }
            }
            completion?.invoke(snapshot)
        }
    }

    val info = FireStoreListenerInfo(this.hashCode(), listenerRegistration, observer?.simpleName)
    FireStoreManager.addListener(info)
}

fun <T> Query.get(
    clazz: Class<T>,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    completion: ((QuerySnapshot?) -> Unit)? = null
) where T : RealmCycle, T : RealmObject {

    get().addOnSuccessListener {
        coroutineScope.launch {
            val data: MutableList<Map<String, Any>> = mutableListOf()

            for (doc in it.documents) {
                if (doc.exists()) {
                    doc.data?.let {
                        data.add(it)
                    }
                }
            }
            RealmManager.create(clazz, data)
            completion?.invoke(it)
        }
    }
}

fun Query.remove() {
    FireStoreManager.removeListener(this)
}