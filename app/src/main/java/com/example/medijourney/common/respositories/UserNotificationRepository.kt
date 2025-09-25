package com.example.medijourney.common.respositories

import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.extensions.RQueryBuilder
import com.example.medijourney.common.managers.fire_store.FSQueryBuilder
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.firebase_auth.FAManger
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.UserNotification
import com.google.firebase.firestore.Query
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface UserNotificationRepository {
    suspend fun observeLatestNotifications(): Flow<List<MutableMap<String, Any>>>
    suspend fun getOlderNotifications(cursorCreatedAt: Long) : List<MutableMap<String, Any>>
    suspend fun observeOlderNotifications(cursorCreatedAt: Long) : Flow<List<MutableMap<String, Any>>>
    fun getUserNotificationsFlow(): Flow<List<UserNotification>>
    suspend fun deleteNotification(id: String): Boolean
    suspend fun deleteAllNotifications(): Boolean
}

class UserNotificationRepositoryImpl @Inject constructor() : UserNotificationRepository {
    override suspend fun observeLatestNotifications(): Flow<List<MutableMap<String, Any>>> {
        return FireStoreManager.getQuerySnapshotFlow(
            FireStoreCollection.USER_NOTIFICATIONS,
            queryBuilder = FSQueryBuilder()
                .orderBy(Constants.CREATED_AT, Query.Direction.DESCENDING)
                .limit(Constants.DEFAULT_LIMIT)
        ).map { snapshot ->
            FireStoreManager.handleQuerySnapshot(UserNotification(), querySnapshot = snapshot)
            snapshot?.documents
                ?.mapNotNull { it.data as? MutableMap<String, Any> }
                .orEmpty()
        }
    }

    override suspend fun getOlderNotifications(cursorCreatedAt: Long): List<MutableMap<String, Any>> {
        val snapshot = FireStoreManager.getCollection(
            FireStoreCollection.USER_NOTIFICATIONS,
            queryBuilder = FSQueryBuilder()
                .lessThan(Constants.CREATED_AT, cursorCreatedAt)
                .orderBy(Constants.CREATED_AT, Query.Direction.DESCENDING)
                .limit(Constants.DEFAULT_LIMIT)
        )
        val list = snapshot.documents.mapNotNull { it.data as? MutableMap<String, Any> }
        RealmManager.write(UserNotification(), list)
        return list
    }

    override suspend fun observeOlderNotifications(cursorCreatedAt: Long): Flow<List<MutableMap<String, Any>>> {
        return FireStoreManager.getQuerySnapshotFlow(
            FireStoreCollection.USER_NOTIFICATIONS,
            queryBuilder = FSQueryBuilder()
                .lessThan(Constants.CREATED_AT, cursorCreatedAt)
                .orderBy(Constants.CREATED_AT, Query.Direction.DESCENDING)
                .limit(Constants.DEFAULT_LIMIT)
            ).map { snapshot ->
            FireStoreManager.handleQuerySnapshot(UserNotification(), querySnapshot = snapshot)
            snapshot?.documents
                ?.mapNotNull { it.data as? MutableMap<String, Any> }
                .orEmpty()
        }
    }

    override fun getUserNotificationsFlow(): Flow<List<UserNotification>> {
        return RealmManager.flow(
            UserNotification::class,
            queryBuilder = RQueryBuilder()
                .sort(UserNotification::createdAt.name, Sort.DESCENDING)
        )
    }

    override suspend fun deleteNotification(id: String): Boolean {
        return FireStoreManager.deleteDoc(
            FireStoreCollection.USER_NOTIFICATIONS,
            id
        )
    }

    override suspend fun deleteAllNotifications(): Boolean {
        return FireStoreManager.deleteCollection(
            FireStoreCollection.USER_NOTIFICATIONS,
            queryBuilder = FSQueryBuilder()
                .equalTo("user_id", FAManger.currentUserCode)
        )
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class UserNotificationModule {

    @Binds
    abstract fun bindUserNotificationRepository(
        impl: UserNotificationRepositoryImpl
    ): UserNotificationRepository

}