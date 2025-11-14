@file:Suppress("UNUSED")
package com.example.medijourney.common.respositories

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class RealmObjectModule {

    @Binds
    abstract fun bindAdvertisementRepo(
        advertisementImpl: AdvertisementRepoImpl
    ): AdvertisementRepo

    @Binds
    abstract fun bindConversationRepo(
        impl: ConversationRepoImpl): ConversationRepo

    @Binds
    abstract fun bindExerciseLevelRepo(
        impl: ExerciseLevelRepoImpl
    ): ExerciseLevelRepo

    @Binds
    abstract fun bindExerciseRepo(
        impl: ExerciseRepoImpl
    ): ExerciseRepo

    @Binds
    abstract fun bindFitnessTrackerActivityRepo(
        impl: FitnessTrackerActivityRepoImpl
    ): FitnessTrackerActivityRepo

    @Binds
    abstract fun bindHospitalRepo(
        impl: HospitalRepoImpl
    ): HospitalRepo

    @Binds
    abstract fun bindMedicalProductRepo(
        impl: MedicalProductRepoImpl
    ): MedicalProductRepo

    @Binds
    abstract fun bindMembershipRepo(
        impl: MembershipRepoImpl): MembershipRepo

    @Binds
    abstract fun bindMessageRepo(
        impl: MessageRepoImpl): MessageRepo

    @Binds
    abstract fun bindRecipeRepo(
        impl: RecipeRepoImpl
    ): RecipeRepo

    @Binds
    abstract fun bindUserExercisePlanRepo(
        impl: UserExercisePlanRepoImpl
    ): UserExercisePlanRepo

    @Binds
    abstract fun bindUserExerciseTrackingReportRepo(
        impl: UserExerciseTrackingReportRepoImpl
    ): UserExerciseTrackingReportRepo

    @Binds
    abstract fun bindUserFitnessTrackerRepo(
        impl: UserFitnessTrackerRepoImpl
    ): UserFitnessTrackerRepo

    @Binds
    abstract fun bindUserMembershipRepo(
        impl: UserMembershipRepoImpl
    ): UserMembershipRepo

    @Binds
    abstract fun bindUserNotificationRepo(
        impl: UserNotificationRepoImpl
    ): UserNotificationRepo

    @Binds
    abstract fun bindUserNutritionTrackingReportRepo(
        impl: UserNutritionTrackingReportRepoImpl
    ): UserNutritionTrackingReportRepo

    @Binds
    abstract fun bindUserRecommendExerciseRepo(
        impl: UserRecommendExerciseRepoImpl
    ): UserRecommendExerciseRepo

    @Binds
    abstract fun bindUserRecommendRecipeRepo(
        impl: UserRecommendRecipeRepoImpl
    ): UserRecommendRecipeRepo

    @Binds
    abstract fun bindUserRepo(
        userRepoImpl: UserRepoImpl
    ): UserRepo

    @Binds
    abstract fun bindUserSettingRepo(
        userSettingImpl: UserSettingRepoImpl
    ): UserSettingRepo

    @Binds
    abstract fun bindUserSleepTrackingReportRepo(
        impl: UserSleepTrackingReportRepoImpl
    ): UserSleepTrackingReportRepo
}