package com.example.medijourney.modules.base.main

import com.example.medijourney.common.managers.fire_store.FSFilterBuilder
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.interfaces.FirestoreListenerInterface
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

interface MainActivityListener : FirestoreListenerInterface

class MainActivityListenerImpl @Inject constructor() : MainActivityListener {

    override fun observe(coroutine: CoroutineScope) {
        coroutine.launch {
            observeHighPriority(coroutine)

            launch {
                delay(5000L)
                observeMediumPriority(coroutine)
            }

            launch {
                delay(10000L)
                observeLowPriority(coroutine)
            }

            launch {
                delay(20000L)
                FireStoreManager.observeRedundantData()
            }
        }
    }

    private fun observeHighPriority(coroutine: CoroutineScope) {
        val userCode = FirebaseAuthManager.getCurrentUserCode() ?: return

        coroutine.apply {
            launch {
                FireStoreManager.observeDoc(FireStoreCollection.USER_MEMBERS, userCode)
            }
            launch {
                FireStoreManager.observeCollection(
                    FireStoreCollection.USER_SETTINGS,
                    filterBuilder = FSFilterBuilder().equalTo("user_id", userCode)
                )
            }
            launch {
                FireStoreManager.observeCollection(FireStoreCollection.ADVERTISEMENTS)
            }
        }
    }

    private fun observeMediumPriority(coroutine: CoroutineScope) {
        val userCode = FirebaseAuthManager.getCurrentUserCode() ?: return

        coroutine.apply {
            launch {
                FireStoreManager.observeCollection(
                    FireStoreCollection.USER_MEDICAL_PRODUCTS,
                    filterBuilder = FSFilterBuilder().equalTo("user_id", userCode)
                )
            }
            launch {
                FireStoreManager.observeCollection(
                    FireStoreCollection.DOCTORS_APPOINTMENTS,
                    filterBuilder = FSFilterBuilder().equalTo("patient_id", userCode)
                )
            }
        }
    }

    private fun observeLowPriority(coroutine: CoroutineScope) {
        val userCode = FirebaseAuthManager.getCurrentUserCode() ?: return

        coroutine.apply {
            launch {
                FireStoreManager.observeCollection(FireStoreCollection.MEDICAL_SPECIALTIES)
            }
            launch {
                FireStoreManager.observeCollection(FireStoreCollection.MEDICAL_SUB_SPECIALTIES)
            }
            launch {
                FireStoreManager.observeCollection(
                    FireStoreCollection.USER_MEDICAL_SPECIALTIES,
                    filterBuilder = FSFilterBuilder()
                        .equalTo("user_id", userCode)
                )
            }
        }
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class MainActivityListenerModule {

    @Binds
    abstract fun bindMainActivityListenerImpl(
        mainActivityListenerImpl: MainActivityListenerImpl
    ): MainActivityListener
}