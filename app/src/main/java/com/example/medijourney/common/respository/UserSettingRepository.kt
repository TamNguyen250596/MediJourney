package com.example.medijourney.common.respository

import com.example.medijourney.common.extensions.toFlow
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.UserSetting
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface UserSettingRepository {
    fun getUserSettings(): Flow<List<UserSetting>>
}

class UserSettingImpl @Inject constructor() : UserSettingRepository {
    override fun getUserSettings(): Flow<List<UserSetting>> {
        return RealmManager
            .query(UserSetting::class.java)
            .toFlow()
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class UserSettingModule {

    @Binds
    abstract fun bindUserSettingRepository(
        userSettingImpl: UserSettingImpl
    ): UserSettingRepository
}