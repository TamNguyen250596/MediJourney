package com.example.medijourney.common.respository

import com.example.medijourney.common.extensions.equalTo
import com.example.medijourney.common.extensions.toFlow
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.User
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface UserRepository {
    fun getUser(id: String): Flow<User?>
}

class UserRepositoryImpl @Inject constructor() : UserRepository {
    override fun getUser(id: String): Flow<User?> {
        return RealmManager
            .query(User::class.java)
            .equalTo(User::id.name, id)
            .toFlow()
            .map { it.firstOrNull() }
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class UserRepositoryModule {

    @Binds
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}