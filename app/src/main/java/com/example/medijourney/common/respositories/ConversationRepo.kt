package com.example.medijourney.common.respositories

import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.managers.fire_store.FSQueryBuilder
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.firebase_auth.FAManger
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.Conversation
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface ConversationRepo {
    suspend fun createUserConversation(conversationId: String): Boolean
    fun getConversationFlow(conversationId: String): Flow<Conversation?>
    fun getConversationsFlow(): Flow<List<Conversation>>
    fun getConversationsFlow(includeCurrentUser: Boolean, keyword: String?): Flow<List<Conversation>>
    fun listenConversations(includeCurrentUser: Boolean, keyword: String?, cursor: String?): Flow<List<Map<String, Any>>>
    suspend fun updateConversation(conversationId: String, data: Map<String, Any>)
    suspend fun deleteUserConversation(conversationId: String): Boolean
}

class ConversationRepoImpl @Inject constructor() : ConversationRepo {

    override suspend fun createUserConversation(conversationId: String): Boolean {
        RealmManager.update(
            Conversation::class.java,
            conversationId,
            mapOf("include_current_user" to false)
        )
        val data = FireStoreManager.getDoc(FireStoreCollection.CONVERSATIONS, conversationId).data
        val memberIds = (data?.get("member_ids") as? MutableMap<*, *>)?.toMutableMap()
        val currentUserCode = FAManger.currentUserCode

        return if (memberIds != null) {
            memberIds[currentUserCode] = true
            FireStoreManager.updateDoc(
                FireStoreCollection.CONVERSATIONS,
                conversationId,
                mapOf("member_ids" to memberIds)
            )
        } else {
            false
        }
    }

    override fun getConversationFlow(conversationId: String): Flow<Conversation?> {
        return RealmManager.flow(
                Conversation::class,
                queryBuilder = RQueryBuilder()
                    .equalTo(Conversation::id.name, conversationId)
        ).map { it.firstOrNull() }
    }

    override fun getConversationsFlow(): Flow<List<Conversation>> {
        return RealmManager.flow(
                Conversation::class,
                queryBuilder = RQueryBuilder()
                    .sort(Conversation::tag.name, Sort.ASCENDING)
            )
    }

    override fun getConversationsFlow(
        includeCurrentUser: Boolean,
        keyword: String?
    ): Flow<List<Conversation>> {
        return RealmManager.flow(
            Conversation::class,
            queryBuilder = RQueryBuilder()
                .apply {
                    if (!keyword.isNullOrEmpty()) {
                        contains(Conversation::keywords.name, keyword)
                    }
                    equalTo(Conversation::includeCurrentUser.name, includeCurrentUser)
                }
                .sort(Conversation::tag.name, Sort.ASCENDING)
        )
    }

    override fun listenConversations(
        includeCurrentUser: Boolean,
        keyword: String?,
        cursor: String?
    ): Flow<List<Map<String, Any>>> {
        return FireStoreManager.getDataFlow(
            FireStoreCollection.CONVERSATIONS,
            queryBuilder = FSQueryBuilder()
                .apply {
                    if (includeCurrentUser) {
                        equalTo("member_ids.${FAManger.currentUserCode}", true)
                    }
                    if (!keyword.isNullOrEmpty()) {
                        equalTo("keyword", keyword)
                    }
                    if (cursor != null) {
                        greaterThan("tag", cursor)
                        lessThan("tag", cursor)
                    }
                }
        )
    }

    override suspend fun updateConversation(
        conversationId: String,
        data: Map<String, Any>
    ) {
        RealmManager.update(Conversation::class.java, conversationId, data)
    }

    override suspend fun deleteUserConversation(conversationId: String): Boolean {
        RealmManager.update(
            Conversation::class.java,
            conversationId,
            mapOf("include_current_user" to false)
        )
        val data = FireStoreManager.getDoc(FireStoreCollection.CONVERSATIONS, conversationId).data
        val memberIds = (data?.get("member_ids") as? MutableMap<*, *>)?.toMutableMap()
        val currentUserCode = FAManger.currentUserCode

        return if (memberIds != null) {
            memberIds.remove(currentUserCode)
            FireStoreManager.updateDoc(
                FireStoreCollection.CONVERSATIONS,
                conversationId,
                mapOf("member_ids" to memberIds)
            )
        } else {
            false
        }
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class ConversationModule {

    @Binds
    abstract fun bindConversationRepository(
        impl: ConversationRepoImpl): ConversationRepo
}