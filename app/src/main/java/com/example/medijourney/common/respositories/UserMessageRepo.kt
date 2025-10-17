package com.example.medijourney.common.respositories

import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.managers.fire_store.FSQueryBuilder
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.UserMessage
import com.google.firebase.firestore.Query
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.collections.mapNotNull
import kotlin.collections.orEmpty

interface UserMessageRepo {
    suspend fun observeLatestMessages(conversationId: String): Flow<List<MutableMap<String, Any>>>
    suspend fun getOlderMessages(conversationId: String, cursorCreatedAt: Long) : List<MutableMap<String, Any>>
    suspend fun observeOlderMessages(conversationId: String, cursorCreatedAt: Long) : Flow<List<MutableMap<String, Any>>>
    suspend fun observeLaterMessages(conversationId: String, cursorCreatedAt: Long) : Flow<List<MutableMap<String, Any>>>
    suspend fun getUserMessages(messageId: String) : Flow<List<MutableMap<String, Any>>>
    fun getUserMessageFLow(conversationId: String) : Flow<List<UserMessage>>
    fun getUserMessageFlow(messageId: String) : Flow<List<UserMessage>>
}

class UserMessageRepoImpl @Inject constructor(): UserMessageRepo {
    override suspend fun observeLatestMessages(conversationId: String): Flow<List<MutableMap<String, Any>>> {
        return FireStoreManager.getQuerySnapshotFlow(
            FireStoreCollection.USER_MESSAGES,
            queryBuilder = FSQueryBuilder()
                .equalTo("conversation_id", conversationId)
                .orderBy(Constants.CREATED_AT, Query.Direction.DESCENDING)
                .limit(Constants.DEFAULT_LIMIT)
        ).map { snapshot ->
            FireStoreManager.handleQuerySnapshot(UserMessage(), querySnapshot = snapshot)
            snapshot?.documents
                ?.mapNotNull { it.data as? MutableMap<String, Any> }
                .orEmpty()
        }
    }

    override suspend fun getOlderMessages(
        conversationId: String,
        cursorCreatedAt: Long
    ): List<MutableMap<String, Any>> {
        val snapshot = FireStoreManager.getCollection(
            FireStoreCollection.USER_MESSAGES,
            queryBuilder = FSQueryBuilder()
                .equalTo("conversation_id", conversationId)
                .lessThan(Constants.CREATED_AT, cursorCreatedAt)
                .orderBy(Constants.CREATED_AT, Query.Direction.DESCENDING)
                .limit(Constants.DEFAULT_LIMIT)
        )
        val list = snapshot.documents.mapNotNull { it.data as? MutableMap<String, Any> }
        RealmManager.write(UserMessage(), list)
        return list
    }

    override suspend fun observeOlderMessages(
        conversationId: String,
        cursorCreatedAt: Long
    ): Flow<List<MutableMap<String, Any>>> {
        return FireStoreManager.getQuerySnapshotFlow(
            FireStoreCollection.USER_MESSAGES,
            queryBuilder = FSQueryBuilder()
                .equalTo("conversation_id", conversationId)
                .lessThan(Constants.CREATED_AT, cursorCreatedAt)
                .orderBy(Constants.CREATED_AT, Query.Direction.DESCENDING)
                .limit(Constants.DEFAULT_LIMIT)
        ).map {
            FireStoreManager.handleQuerySnapshot(UserMessage(), querySnapshot = it)
            it?.documents
                ?.mapNotNull { it.data as? MutableMap<String, Any> }
                .orEmpty()
        }
    }

    override suspend fun observeLaterMessages(
        conversationId: String,
        cursorCreatedAt: Long
    ): Flow<List<MutableMap<String, Any>>> {
        return FireStoreManager.getQuerySnapshotFlow(
            FireStoreCollection.USER_MESSAGES,
            queryBuilder = FSQueryBuilder()
                .equalTo("conversation_id", conversationId)
                .greaterThan(Constants.CREATED_AT, cursorCreatedAt)
                .orderBy(Constants.CREATED_AT, Query.Direction.DESCENDING)
                .limit(Constants.DEFAULT_LIMIT)
        ).map {
            FireStoreManager.handleQuerySnapshot(UserMessage(), querySnapshot = it)
            it?.documents
                ?.mapNotNull { it.data as? MutableMap<String, Any> }
                .orEmpty()
        }
    }

    override suspend fun getUserMessages(messageId: String): Flow<List<MutableMap<String, Any>>> {
        return FireStoreManager.getQuerySnapshotFlow(
            FireStoreCollection.USER_MESSAGES,
            queryBuilder = FSQueryBuilder()
                .equalTo("message_id", messageId)
        ).map {
            FireStoreManager.handleQuerySnapshot(UserMessage(), querySnapshot = it)
            it?.documents
                ?.mapNotNull { it.data as? MutableMap<String, Any> }
                .orEmpty()
        }
    }

    override fun getUserMessageFLow(conversationId: String): Flow<List<UserMessage>> {
        return RealmManager.flow(
            UserMessage::class,
            queryBuilder = RQueryBuilder()
                .equalTo(UserMessage::conversationId.name, conversationId)
                .sort(UserMessage::createdAt.name, Sort.DESCENDING)
        )
    }

    override fun getUserMessageFlow(messageId: String): Flow<List<UserMessage>> {
        return RealmManager.flow(
            UserMessage::class,
            queryBuilder = RQueryBuilder()
                .equalTo(UserMessage::messageId.name, messageId)
        )
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class UserMessageModule {

    @Binds
    abstract fun bindUserMessageRepository(
    impl: UserMessageRepoImpl): UserMessageRepo
}