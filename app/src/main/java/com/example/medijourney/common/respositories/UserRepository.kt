package com.example.medijourney.common.respositories

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
    fun getUserFlow(id: String): Flow<User?>
}

class UserRepositoryImpl @Inject constructor() : UserRepository {
    override fun getUserFlow(id: String): Flow<User?> {
        return RealmManager.flow(User::class,).map { it.firstOrNull() }
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