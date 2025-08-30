package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getRealmInstant
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.fire_store.addListener
import com.example.medijourney.common.managers.realm.RealmCycle
import com.example.medijourney.common.managers.realm.RealmManager
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserMessage: RealmObject, RealmCycle {

    // Properties
    @PrimaryKey
    var id: String = ""
    var createdAt: RealmInstant? = null
    var messageId: String = ""
    var message: Message? = null
    var conversationId: String = ""

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun create(map: Map<String, Any>): RealmObject {
        return UserMessage().apply {
            id = map["id"] as? String ?: ""
            createdAt = map.getRealmInstant("created_at")
            messageId = map["message_id"] as? String ?: ""
            conversationId = map["conversation_id"] as? String ?: ""
        }
    }

    override fun update(map: Map<String, Any>) {
        messageId = map["message_id"] as? String ?: messageId
    }

    override fun didInit(map: Map<String, Any>) {
        super.didInit(map)

        val messageId = map["message_id"] as? String ?: return
        val id = map["id"] as? String ?: return

        CoroutineScope(Dispatchers.IO).launch {
            RealmManager.createRealm().write {
                val message = query(
                    Message::class,
                    "${Message::id.name}== $0", messageId)
                    .find()
                    .firstOrNull()
                val userMessage = query(
                    UserMessage::class,
                    "${UserMessage::id.name} == $0", id)
                    .find().
                    firstOrNull()
                userMessage?.message = message
            }
        }

        FireStoreManager.buildDoc(FireStoreCollection.MESSAGES to messageId)
            .addListener {
                val data = it.data
                if (data != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        RealmManager.createRealm().write {
                            val existingMessage = query(
                                Message::class,
                                "${Message::id.name}== $0", messageId)
                                .find()
                                .firstOrNull()
                            if (existingMessage == null) {
                                val message = Message().create(data) as? Message
                                val userMessage = query(
                                    UserMessage::class,
                                    "${UserMessage::id.name} == $0", id)
                                    .find().
                                    firstOrNull()
                                message?.let {
                                    userMessage?.message = copyToRealm(message)
                                }
                            } else {
                                existingMessage.update(data)
                            }
                        }
                    }
                }
            }
    }

    override fun removeDependencies() {
        super.removeDependencies()
        if (!isValid()) return

        val query =  FireStoreManager.buildDoc(FireStoreCollection.MESSAGES to messageId)
        FireStoreManager.removeListener(query)
    }
}