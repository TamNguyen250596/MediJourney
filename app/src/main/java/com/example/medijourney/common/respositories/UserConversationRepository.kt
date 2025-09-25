package com.example.medijourney.common.respositories

import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.managers.fire_store.FSQueryBuilder
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.UserConversation
import com.google.firebase.firestore.Query
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.collections.orEmpty

interface UserConversationRepository {
    suspend fun observeUserConversations(): Flow<List<MutableMap<String, Any>>>
    suspend fun getUserConversationsPageAfterTag(tag: String) : List<MutableMap<String, Any>>
    suspend fun observeUserConversationsPageAfterTag(tag: String): Flow<List<MutableMap<String, Any>>>
    suspend fun observeUserConversationsPageBeforeTag(tag: String): Flow<List<MutableMap<String, Any>>>
    fun getUserConversationsFlow(userConversationId: String?): Flow<List<UserConversation>>
}

class UserConversationRepositoryImpl @Inject constructor() : UserConversationRepository {
    override suspend fun observeUserConversations(): Flow<List<MutableMap<String, Any>>> {
        return FireStoreManager.getQuerySnapshotFlow(
            FireStoreCollection.USER_CONVERSATIONS,
            queryBuilder = FSQueryBuilder()
                .orderBy("tag", Query.Direction.ASCENDING)
                .limit(Constants.DEFAULT_LIMIT)
        ).map { snapshot ->
            FireStoreManager.handleQuerySnapshot(UserConversation(), querySnapshot = snapshot)
            snapshot?.documents
                ?.mapNotNull { it.data as? MutableMap<String, Any> }
                .orEmpty()
        }
    }

    override suspend fun getUserConversationsPageAfterTag(tag: String): List<MutableMap<String, Any>> {
        val snapshot = FireStoreManager.getCollection(
            FireStoreCollection.USER_CONVERSATIONS,
            queryBuilder = FSQueryBuilder()
                .orderBy("tag", Query.Direction.ASCENDING)
                .greaterThan("tag", tag)
                .limit(Constants.DEFAULT_LIMIT)
        )
        val list = snapshot.documents.mapNotNull { it.data as? MutableMap<String, Any> }
        RealmManager.write(UserConversation(), list)
        return list
    }

    override suspend fun observeUserConversationsPageAfterTag(tag: String): Flow<List<MutableMap<String, Any>>> {
        return FireStoreManager.getQuerySnapshotFlow(
            FireStoreCollection.USER_CONVERSATIONS,
            queryBuilder = FSQueryBuilder()
                .orderBy("tag", Query.Direction.ASCENDING)
                .greaterThan("tag", tag)
                .limit(Constants.DEFAULT_LIMIT)
            ).map { snapshot ->
            FireStoreManager.handleQuerySnapshot(UserConversation(), querySnapshot = snapshot)
            snapshot?.documents
                ?.mapNotNull { it.data as? MutableMap<String, Any> }
                .orEmpty()
        }
    }

    override suspend fun observeUserConversationsPageBeforeTag(tag: String): Flow<List<MutableMap<String, Any>>> {
        return FireStoreManager.getQuerySnapshotFlow(
            FireStoreCollection.USER_CONVERSATIONS,
            queryBuilder = FSQueryBuilder()
                .orderBy("tag", Query.Direction.DESCENDING)
                .lessThan("tag", tag)
                .limit(Constants.DEFAULT_LIMIT)
            ).map { snapshot ->
            FireStoreManager.handleQuerySnapshot(UserConversation(), querySnapshot = snapshot)
            snapshot?.documents
                ?.mapNotNull { it.data as? MutableMap<String, Any> }
                .orEmpty()
        }
    }

    override fun getUserConversationsFlow(userConversationId: String?): Flow<List<UserConversation>> {
        return RealmManager.flow(
            UserConversation::class,
            queryBuilder = RQueryBuilder()
                .apply {
                    if (userConversationId != null) {
                        equalTo(UserConversation::id.name, userConversationId)
                    }
                }
                .sort(UserConversation::tag.name, Sort.ASCENDING)
        )
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class UserConversationModule {

    @Binds
    abstract fun bindUserConversationRepository(
        impl: UserConversationRepositoryImpl): UserConversationRepository
}