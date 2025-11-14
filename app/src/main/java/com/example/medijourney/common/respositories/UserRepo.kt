package com.example.medijourney.common.respositories

import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface UserRepo {
    fun getUserFlow(id: String): Flow<User?>
}

class UserRepoImpl @Inject constructor() : UserRepo {
    override fun getUserFlow(id: String): Flow<User?> {
        return RealmManager.flow(User::class,).map { it.firstOrNull() }
    }
}

//@Module
//@InstallIn(ViewModelComponent::class)
//abstract class UserRepositoryModule {
//
//    @Binds
//    abstract fun bindUserRepository(
//        userRepositoryImpl: UserRepoImpl
//    ): UserRepo
//}