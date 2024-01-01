package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.fire_store.addListener
import com.example.medijourney.common.managers.realm.RealmCycle
import com.example.medijourney.common.managers.realm.RealmManager
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserConversation: RealmObject, RealmCycle {

    // Properties
    @PrimaryKey
    var id: String = ""
    var userCode: String = ""
    var tag: String = ""
    var conversationId: String = ""
    var conversation: Conversation? = null

    // Content
    override fun primaryKey(): String {
        return "id"
    }

    override fun toRealmObject(map: Map<String, Any>): RealmObject {
        return UserConversation().apply {
            id = map["id"] as? String ?: id
            userCode = map["user_code"] as? String ?: userCode
            tag = map["tag"] as? String ?: tag
            conversationId = map["conversation_id"] as? String ?: conversationId
        }
    }

    override fun updateFromMap(map: Map<String, Any>) {
        tag = map["tag"] as? String ?: tag
    }

    override fun handleDependencies(map: Map<String, Any>) {
        super.handleDependencies(map)

        val conversationId = map["conversation_id"] as? String ?: return
        val id = map["id"] as? String ?: return

        CoroutineScope(Dispatchers.IO).launch {
            RealmManager.createRealm().write {
                val conversation = query(
                    Conversation::class,
                    "${Conversation::id.name} == $0", conversationId)
                    .find()
                    .firstOrNull()
                val userConversation = query(
                    UserConversation::class,
                    "${UserConversation::id.name} == $0", id)
                    .find()
                    .firstOrNull()
                userConversation?.conversation = conversation
            }
        }

        FireStoreManager.buildDocRef(FireStoreCollection.CONVERSATIONS to conversationId)
            .addListener {
                val data = it.data
                if (data != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        RealmManager.createRealm().write {
                            val existingConversation = query(
                                Conversation::class,
                                "${Conversation::id.name} == $0", conversationId)
                                .find()
                                .firstOrNull()
                            if (existingConversation == null) {
                                val conversation = Conversation().toRealmObject(data) as? Conversation
                                val userConversation = query(
                                    UserConversation::class,
                                    "${UserConversation::id.name} == $0", id)
                                    .find()
                                    .firstOrNull()
                                conversation?.let {
                                    userConversation?.conversation = copyToRealm(conversation)
                                }
                            } else {
                                existingConversation.updateFromMap(data)
                            }
                        }
                    }
                }
            }
    }

    override fun removeDependencies() {
        super.removeDependencies()
        if (!isValid()) return

        val query =  FireStoreManager.buildDocRef(FireStoreCollection.CONVERSATIONS to conversationId)
        FireStoreManager.removeListener(query)
    }
}