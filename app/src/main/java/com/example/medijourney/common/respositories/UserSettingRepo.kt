package com.example.medijourney.common.respositories

import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.UserSetting
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface UserSettingRepo {
    fun getUserSettings(): Flow<List<UserSetting>>
}

class UserSettingRepoImpl @Inject constructor() : UserSettingRepo {
    override fun getUserSettings(): Flow<List<UserSetting>> {
        return RealmManager.flow(UserSetting::class)
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class UserSettingModule {

    @Binds
    abstract fun bindUserSettingRepository(
        userSettingImpl: UserSettingRepoImpl
    ): UserSettingRepo
}